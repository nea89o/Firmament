package moe.nea.jarvis.impl;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class JarvisConfigSearch extends Screen {
    private final JarvisContainer container;
    private final List<ConfigOptionWithCustody> allOptions;
    private final Screen parentScreen;
    private List<ConfigOptionWithCustody> filteredOptions = new ArrayList<>();
    private EditBox searchField;
    private String searchQuery = "";
    private int searchFieldWidth;

    public JarvisConfigSearch(JarvisContainer container, Screen parentScreen, List<ConfigOptionWithCustody> allOptions) {
        super(Component.translatable("jarvis.configlist"));
        this.container = container;
        this.allOptions = allOptions;
        this.parentScreen = parentScreen;
        updateSearchResults("");
    }

    double scroll;

    @Override
    protected void init() {
        assert minecraft != null;
        super.init();
        searchFieldWidth = Math.min(400, width / 3);
        addWidget(searchField = new EditBox(minecraft.font, width / 2 - searchFieldWidth / 2,
            10, searchFieldWidth, 18, Component.translatable("jarvis.configlist.suggestion")));
        searchField.setValue(searchQuery);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        assert minecraft != null;
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0x50000000);
        context.enableScissor(0, 35, width, height);
        context.pose().pushMatrix();

        int left = width / 2 - searchFieldWidth / 2;
        context.pose().translate(left, 35F - (float) scroll);
        mouseY -= 35 - scroll;
        mouseX -= left;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            int height = 15 + filteredOption.option().description().size() * 10;
            if (0 <= mouseX && mouseX < searchFieldWidth &&
                0 <= mouseY && mouseY < height) {
                context.fill(0, 0, searchFieldWidth, height, 0x50A0A0A0);
            }

            context.text(minecraft.font, Component.literal("")
                .append(container.getModName(filteredOption.plugin()))
                .append(Component.literal(" > ")).append(filteredOption.option().title()), 2, 2, -1, false);
            int offset = 15;
            for (var descriptionLine : filteredOption.option().description()) {
                context.text(minecraft.font, descriptionLine, 2, offset, 0xFF808080, true);
                offset += 10;
            }
            mouseY -= height;
            context.pose().translate(0, height);
        }

        context.pose().popMatrix();
        context.disableScissor();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parentScreen);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (35 <= mouseY && mouseY < height && width / 2 - searchFieldWidth / 2 <= mouseX
            && mouseX < width / 2 + searchFieldWidth / 2) {
            scroll(verticalAmount);
            return true;
        }
        return false;
    }

    public void scroll(double amount) {
        int usedHeight = -height + 35;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            usedHeight += filteredOption.option().description().size() * 10 + 15;
        }
        scroll = JarvisUtil.coerce(scroll + amount * -5, 0, usedHeight);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (super.mouseClicked(mouseButtonEvent, bl)) return true;
        var mouseY = mouseButtonEvent.y() - (35 - scroll);
        int left = width / 2 - searchFieldWidth / 2;
        var mouseX = mouseButtonEvent.x() - left;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            int height = 15 + filteredOption.option().description().size() * 10;
            if (0 <= mouseX && mouseX < searchFieldWidth &&
                0 <= mouseY && mouseY < height) {
                minecraft.gui.setScreen(filteredOption.option().jumpTo(this));
                return true;
            }
            mouseY -= height;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        String before = searchField.getValue();
        searchField.setFocused(true);
        boolean ret = super.charTyped(characterEvent);
        String after = searchField.getValue();
        if (!Objects.equals(before, after)) {
            updateSearchResults(after);
        }
        return ret;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        String before = searchField.getValue();
        boolean ret = super.keyPressed(keyEvent);
        String after = searchField.getValue();
        if (!Objects.equals(before, after)) {
            updateSearchResults(after);
        }
        return ret;
    }

    private void updateSearchResults(String searchTerm) {
        searchQuery = searchTerm;
        String[] groups = searchTerm.toLowerCase(Locale.ROOT).split("\\|");
        String[][] filter = new String[groups.length][];
        for (int i = 0; i < groups.length; i++) {
            String[] group = groups[i].trim().split(" +");
            for (int j = 0; j < group.length; j++) {
                group[j] = group[j].toLowerCase(Locale.ROOT);
            }
            filter[i] = group;
        }
        filteredOptions = allOptions.stream()
            .filter(it -> filterOption(it, filter))
            .collect(Collectors.toList());
        scroll(0);
    }

    private List<String> getTextPieces(ConfigOptionWithCustody withCustody) {
        ArrayList<String> objects = new ArrayList<>();
        var option = withCustody.option();
        objects.add(option.title().getString().toLowerCase(Locale.ROOT));
        Component category = option.category();
        if (category != null) {
            objects.add(category.getString().toLowerCase(Locale.ROOT));
        }
        objects.add(container.getModName(withCustody.plugin()).getString().toLowerCase(Locale.ROOT));
        for (var text : option.description()) {
            objects.add(text.getString().toLowerCase(Locale.ROOT));
        }
        return objects;
    }

    private boolean filterOption(ConfigOptionWithCustody configOptionWithCustody, String[][] searchTerm) {
        List<String> textPieces = getTextPieces(configOptionWithCustody);
        for (String[] group : searchTerm) {
            boolean matched = true;
            for (String part : group) {
                boolean partMatched = configOptionWithCustody.option().match(part);
                if (!partMatched)
                    for (String textPiece : textPieces) {
                        if (textPiece.contains(part)) {
                            partMatched = true;
                            break;
                        }
                    }
                if (!partMatched) {
                    matched = false;
                    break;
                }
            }
            if (matched) return true;
        }
        return false;
    }
}
