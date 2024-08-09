package moe.nea.firmament.init;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Type;

import java.util.List;

public class Intermediary {
    private static final MappingResolver RESOLVER = FabricLoader.getInstance().getMappingResolver();

    static String methodName(Object object) {
        throw new AssertionError("Cannot be called at runtime");
    }

    static <T> String className() {
        throw new AssertionError("Cannot be called at runtime");
    }

    static String id(String source) {
        return source;
    }

//    public record Class(
//        Type intermediaryClass
//    ) {
//        public Class(String intermediaryClass) {
//            this(Type.getObjectType(intermediaryClass.replace('.', '/')));
//        }
//
//        public String getMappedName() {
//            return RESOLVER.mapClassName("intermediary", intermediaryClass.getInternalName()
//                                                                          .replace('/', '.'));
//        }
//    }
//
//    public record Method(
//        Type intermediaryClassName,
//        String intermediaryMethodName,
//        Type intermediaryReturnType,
//        List<Type> intermediaryArgumentTypes
//    ) {
//        public Method(
//            String intermediaryClassName,
//            String intermediaryMethodName,
//            String intermediaryReturnType,
//            String... intermediaryArgumentTypes
//        ) {
//            this(intermediaryClassName, intermediaryMethodName, intermediaryReturnType, List.of(intermediaryArgumentTypes));
//        }
//
//        public String getMappedMethodName() {
//            return RESOLVER.mapMethodName("intermediary",
//                                          intermediaryClassName.getInternalName().replace('/', '.'));
//        }
//
//        public Type getIntermediaryDescriptor() {
//            return Type.getMethodType(intermediaryReturnType, intermediaryArgumentTypes.toArray(Type[]::new));
//        }
//
//
//    }
}
