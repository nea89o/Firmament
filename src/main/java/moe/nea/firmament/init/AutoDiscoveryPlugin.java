package moe.nea.firmament.init;


import moe.nea.firmament.util.ErrorUtil;
import moe.nea.firmament.util.compatloader.ICompatMeta;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AutoDiscoveryPlugin {
	public static List<String> getDefaultAllMixinClassesFQNs() {
		var defaultName = "moe.nea.firmament.mixins";
		var plugin = new AutoDiscoveryPlugin();
		plugin.setMixinPackage(defaultName);
		var mixins = plugin.getMixins();
		return mixins.stream().map(it -> defaultName + "." + it).toList();
	}

	private static final List<AutoDiscoveryPlugin> mixinPlugins = new ArrayList<>();

	public static List<AutoDiscoveryPlugin> getMixinPlugins() {
		return mixinPlugins;
	}

	private String mixinPackage;

	public void setMixinPackage(String mixinPackage) {
		this.mixinPackage = mixinPackage;
		mixinPlugins.add(this);
	}

	/**
	 * Resolves the base class root for a given class URL. This resolves either the JAR root, or the class file root.
	 * In either case the return value of this + the class name will resolve back to the original class url, or to other
	 * class urls for other classes.
	 */
	public URL getBaseUrlForClassUrl(URL classUrl) {
		String string = classUrl.toString();
		if (classUrl.getProtocol().equals("jar")) {
			try {
				return new URL(string.substring(4).split("!")[0]);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		if (string.endsWith(".class")) {
			try {
				return new URL(string.replace("\\", "/")
				                     .replace(getClass().getCanonicalName()
				                                        .replace(".", "/") + ".class", ""));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		return classUrl;
	}

	/**
	 * Get the package that contains all the mixins. This value is set using {@link #setMixinPackage}.
	 */
	public String getMixinPackage() {
		return mixinPackage;
	}

	/**
	 * Get the path inside the class root to the mixin package
	 */
	public String getMixinBaseDir() {
		return mixinPackage.replace(".", "/");
	}

	/**
	 * A list of all discovered mixins.
	 */
	private List<String> mixins = null;

	/**
	 * Try to add mixin class ot the mixins based on the filepath inside of the class root.
	 * Removes the {@code .class} file suffix, as well as the base mixin package.
	 * <p><b>This method cannot be called after mixin initialization.</p>
	 *
	 * @param className the name or path of a class to be registered as a mixin.
	 */
	public void tryAddMixinClass(String className) {
		if (!className.endsWith(".class")) return;
		String norm = (className.substring(0, className.length() - ".class".length()))
			              .replace("\\", "/")
			              .replace("/", ".");
		if (norm.startsWith(getMixinPackage() + ".") && !norm.endsWith(".") && ICompatMeta.Companion.shouldLoad(norm)) {
			mixins.add(norm.substring(getMixinPackage().length() + 1));
		}
	}

	private void tryDiscoverFromContentFile(URL url) {
		Path file;
		try {
			file = Paths.get(getBaseUrlForClassUrl(url).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Base directory found at " + file);
		if (!Files.exists(file)) {
			System.out.println("Skipping non-existing mixin root: " + file);
			return;
		}
		if (Files.isDirectory(file)) {
			walkDir(file);
		} else {
			walkJar(file);
		}
		System.out.println("Found mixins: " + mixins);

	}

	/**
	 * Search through the JAR or class directory to find mixins contained in {@link #getMixinPackage()}
	 */
	public List<String> getMixins() {
		if (mixins != null) return mixins;
		try {
			System.out.println("Trying to discover mixins");
			mixins = new ArrayList<>();
			URL classUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
			System.out.println("Found classes at " + classUrl);
			tryDiscoverFromContentFile(classUrl);
			var classRoots = System.getProperty("firmament.classroots");
			if (classRoots != null && !classRoots.isBlank()) {
				System.out.println("Found firmament class roots: " + classRoots);
				for (String s : classRoots.split(File.pathSeparator)) {
					if (s.isBlank()) {
						continue;
					}
					tryDiscoverFromContentFile(new File(s).toURI().toURL());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return mixins;
	}

	/**
	 * Search through directory for mixin classes based on {@link #getMixinBaseDir}.
	 *
	 * @param classRoot The root directory in which classes are stored for the default package.
	 */
	private void walkDir(Path classRoot) {
		System.out.println("Trying to find mixins from directory");
		var path = classRoot.resolve(getMixinBaseDir());
		if (!Files.exists(path)) return;
		try (Stream<Path> classes = Files.walk(path)) {
			classes.map(it -> classRoot.relativize(it).toString())
			       .forEach(this::tryAddMixinClass);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read through a JAR file, trying to find all mixins inside.
	 */
	private void walkJar(Path file) {
		System.out.println("Trying to find mixins from jar file");
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file))) {
			ZipEntry next;
			while ((next = zis.getNextEntry()) != null) {
				tryAddMixinClass(next.getName());
				zis.closeEntry();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
