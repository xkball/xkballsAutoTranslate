package com.xkball.auto_translate.client.gui.screen;

import com.xkball.auto_translate.AutoTranslate;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.utils.VanillaUtils;
import com.xkball.xklibmc.ui.widget.mc.ObjectInputBox;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.ClientUtils;
import com.xkball.auto_translate.utils.translate.LangKeyTranslateUnit;
import com.xkball.auto_translate.utils.translate.TranslatorType;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XATConfigScreen extends XKLibBaseScreen {

    @Nullable
    private final Screen parentScreen;
    private final LangKeyTranslateUnit translateUnit = new LangKeyTranslateUnit();
    private ContainerWidget scrollContent;
    private ContainerWidget processingBarRow;
    private ContainerWidget runTransKeysRow;
    private WidgetWrapper syncButton;

    public XATConfigScreen(@Nullable ModContainer container, @Nullable Screen parent) {
        super(Component.empty());
        this.parentScreen = parent;
        AutoTranslate.onUpdate = this::onDynamicUpdate;
        
        var root = new ContainerWidget();
        root.inlineStyle("size: 100% 100%; flex-direction: column;");
        root.asRootStyle("""
                * {
                    flex-shrink: 0;
                }
                Label {
                    text-color: -1;
                    text-height: 10rpx;
                }
                """);
        
        root.addChild(createTitleBar());
        
        scrollContent = new ContainerWidget();
        scrollContent.inlineStyle("""
                size: 100% 100%-36rpx;
                flex-direction: column;
                overflow-y: scroll;
                scrollbar-width: 8;
                """);
        
        scrollContent.addChild(createConfigTitle("xat.gui.config.title.network"));
        scrollContent.addChild(createEntry("xat.gui.config.http_host", XATConfig.HTTP_PROXY_HOST_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)));
        scrollContent.addChild(createEntry("xat.gui.config.http_port", XATConfig.HTTP_PROXY_PORT_CONFIG, () -> createInput(ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER)));
        scrollContent.addChild(createEntry("xat.gui.config.max_retries", XATConfig.MAX_RETRIES_CONFIG, () -> createInput(ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER)));
        scrollContent.addChild(createConfigTitle("xat.gui.config.title.translator"));
        scrollContent.addChild(createEntryEnum("xat.gui.config.translator", XATConfig.TRANSLATOR_TYPE_CONFIG, TranslatorType.class));
        scrollContent.addChild(createTargetEntry());
        scrollContent.addChild(createConfigTitle("xat.gui.config.title.llm_config"));
        scrollContent.addChild(createEntry("xat.gui.config.llm_api_url", XATConfig.LLM_API_URL_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)));
        scrollContent.addChild(createEntry("xat.gui.config.llm_model", XATConfig.LLM_MODEL_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)));
        scrollContent.addChild(createAPIKeyInput());
        scrollContent.addChild(createConfigTitle("xat.gui.run_trans_keys"));
        scrollContent.addChild(addNotice("xat.gui.config.run_trans_notice"));
        scrollContent.addChild(createRunTransKeys());
        scrollContent.addChild(createProcessingBar());
        scrollContent.addChild(createConfigTitle("xat.gui.config.title.others"));
        scrollContent.addChild(createTokenCostLabel());
        scrollContent.addChild(createClearAllButton());
        
        root.addChild(scrollContent);
        
        root.addChild(createBottomBar());
        
        this.addScreenLayer(root);
    }

    @Override
    protected void init() {
        super.init();
    }

    private ContainerWidget createTitleBar() {
        var bar = new ContainerWidget();
        bar.inlineStyle("""
                size: 100% 24rpx;
                display: flex;
                flex-direction: row;
                align-items: center;
                flex-shrink: 0;
                justify-content: center;
                background-color: 0xaa222222;
                """);

        var title = new Label(IComponent.translatable("xat.gui.config"));
        title.inlineStyle("size: content 20rpx; text-align: center; text-scale: expand-width;");
        bar.addChild(title);

        var openConfigBtn = new WidgetWrapper(
                net.minecraft.client.gui.components.Button.builder(
                        Component.translatable("xat.gui.open_config_file"),
                        _ -> {
                            try {
                                var file = FMLPaths.CONFIGDIR.get().resolve("xkball_s_auto_translate-common.toml").toFile();
                                Util.getPlatform().openFile(file);
                            } catch (Exception ignored) {}
                        }
                ).bounds(0, 0, 20, 20).build()
        );
        openConfigBtn.inlineStyle("size: 10% 20rpx; min-width: 20rpx;margin-left: auto; margin-right: 10rpx;");
        bar.addChild(openConfigBtn);

        return bar;
    }

    private ContainerWidget createBottomBar() {
        var bar = new ContainerWidget();
        bar.inlineStyle("size: 100% 16rpx; flex-shrink: 0; background-color: 0xaa222222;");
        return bar;
    }

    private Label createConfigTitle(String key) {
        var label = new Label(IComponent.translatable(key));
        label.inlineStyle("margin-top: 12rpx; margin-left: 20%;");
        return label;
    }

    private ContainerWidget addNotice(String key) {
        var row = new ContainerWidget();
        row.inlineStyle("display: flex; flex-direction: row; height: 10rpx;");
        var label = new Label(IComponent.translatable(key));
        label.inlineStyle("size: 100% auto;");
        row.addChild(label);
        return row;
    }

    private ContainerWidget createTokenCostLabel() {
        var row = new ContainerWidget();
        row.inlineStyle("display: flex; flex-direction: row; height: 10rpx;");
        var label = new Label(IComponent.translatable("xat.gui.token_cost", XATDataBase.INSTANCE.getTokenCost()));
        label.inlineStyle("size: 100% auto;");
        row.addChild(label);
        return row;
    }

    private ContainerWidget createClearAllButton() {
        var row = new ContainerWidget();
        row.inlineStyle("display: flex; flex-direction: row; height: 40rpx;");
        var btn = WidgetWrapper.button(Component.translatable("xat.gui.btn.clear_all_cache"), b -> XATDataBase.INSTANCE.clearAllTranslateCache());
        btn.inlineStyle("size: 100% 20rpx; margin-left: 8rpx; margin-right: 8rpx;");
        row.addChild(btn);
        return row;
    }

    private ContainerWidget createProcessingBar() {
        processingBarRow = new ContainerWidget();
        processingBarRow.inlineStyle("display: flex; flex-direction: row; align-items: center; height: 10rpx;");
        rebuildProcessingBar();
        return processingBarRow;
    }

    private void rebuildProcessingBar() {
        processingBarRow.clearChildren();
        var finishedLabel = new Label(IComponent.literal(I18n.get("xat.gui.processing") + translateUnit.normalFinishedSize() + "/" + translateUnit.size()));
        finishedLabel.inlineStyle("size: 40% auto; margin-right: 4rpx;");
        processingBarRow.addChild(finishedLabel);

        var errorLabel = new Label(IComponent.literal(I18n.get("xat.gui.error") + translateUnit.errorSize() + "/" + translateUnit.size()));
        errorLabel.inlineStyle("size: 40% auto; margin-left: 4rpx;");
        processingBarRow.addChild(errorLabel);
    }

    private ContainerWidget createRunTransKeys() {
        runTransKeysRow = new ContainerWidget();
        runTransKeysRow.inlineStyle("display: flex; flex-direction: row; align-items: center; height: 40rpx;");
        rebuildRunTransKeysRow();
        return runTransKeysRow;
    }

    private void rebuildRunTransKeysRow() {
        runTransKeysRow.clearChildren();

        var btn1 = WidgetWrapper.button(Component.translatable("xat.gui.btn.run_trans_keys"), b -> {
            if (translateUnit.finished) runTransKeys();
        });
        btn1.enabled = translateUnit.finished;
        btn1.inlineStyle("size: 25% 20rpx; margin-left: 4rpx; margin-right: 4rpx;");
        runTransKeysRow.addChild(btn1);

        var btn2 = WidgetWrapper.button(Component.translatable("xat.gui.btn.cancel_inject_lang"), b -> {
            translateUnit.cancel();
            XATDataBase.INSTANCE.enableInjectLang(false);
            AutoTranslate.cancelInjectLanguage();
            onDynamicUpdate();
        });
        btn2.enabled = XATDataBase.INSTANCE.isEnableInjectLang();
        btn2.inlineStyle("size: 25% 20rpx; margin-left: 4rpx; margin-right: 4rpx;");
        runTransKeysRow.addChild(btn2);

        var btn3 = WidgetWrapper.button(Component.translatable("xat.gui.btn.clear_cache"), b -> LangKeyTranslateUnit.I18N_KEYS.clear());
        btn3.inlineStyle("size: 25% 20rpx; margin-left: 4rpx; margin-right: 4rpx;");
        runTransKeysRow.addChild(btn3);
    }

    public void runTransKeys() {
        this.translateUnit.reset();
        onDynamicUpdate();
        XATDataBase.INSTANCE.enableInjectLang(true);
        var en = ClientUtils.getClientLanguage("en_us");
        var target = ClientUtils.getClientLanguage(XATConfig.TARGET_LANGUAGE_CONFIG.get()).getLanguageData().keySet();
        var diff = en.getLanguageData().entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty()
                        && LangKeyTranslateUnit.I18N_KEYS.get(entry.getKey()) == null
                        && !target.contains(entry.getKey())
                ).toList();
        if (diff.isEmpty()) {
            AutoTranslate.injectLanguage();
            return;
        }
        IntStream.range(0, diff.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> i / 20))
                .values().stream()
                .map(indexes -> indexes.stream().map(diff::get).toList())
                .forEach(translateUnit::submitRequest);
        this.translateUnit.start();
    }

    private <T> WidgetWrapper saveButton(Supplier<T> supplier, ModConfigSpec.ConfigValue<T> config) {
        var btn = SpriteIconButton.builder(Component.empty(), _ -> {
                    var t = supplier.get();
                    if (t != null) {
                        config.set(t);
                        config.save();
                    }
                }, true)
                .sprite(VanillaUtils.modRL("icon/save"), 16, 16)
                .build();
        var wrapper = new WidgetWrapper(btn);
        wrapper.inlineStyle("size: 20rpx 20rpx;");
        return wrapper;
    }

    private <T> ContainerWidget createEntry(String key, ModConfigSpec.ConfigValue<T> config, Supplier<ObjectInputBox<T>> inputBSupplier) {
        var input = inputBSupplier.get();
        setupEditBox(input);
        input.setValue(config.get().toString());
        input.scrollTo(0);
        return createEntry_(key, wrapInput(input), saveButton(input::get, config));
    }

    private ContainerWidget createTargetEntry() {
        var input = createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
        var config = XATConfig.TARGET_LANGUAGE_CONFIG;
        setupEditBox(input);
        input.setValue(config.get());
        input.scrollTo(0);

        syncButton = new WidgetWrapper(
                net.minecraft.client.gui.components.Button.builder(
                        Component.literal("S"),
                        _ -> {
                            input.setValue(Minecraft.getInstance().getLanguageManager().getSelected());
                            config.set(input.getValue());
                            config.save();
                        }
                ).bounds(0, 0, 20, 20).build()
        );
        syncButton.inlineStyle("size: 20rpx 20rpx;");

        var inputWrapper = wrapInput(input).inlineStyle("width: 100%-28rpx;");

        var panel = new ContainerWidget();
        panel.inlineStyle("align-items: center; size: 100%-20rpx 20rpx; flex-grow: 1; margin-right: 8rpx;");
        panel.addChild(inputWrapper);
        panel.addChild(syncButton);

        return createEntry_("xat.gui.config.target", panel, saveButton(input::get, config));
    }

    private <T extends Enum<T>> ContainerWidget createEntryEnum(String key, ModConfigSpec.ConfigValue<T> config, Class<T> enumValue) {
        var btn = new CycleButton.Builder<T>((t) -> Component.literal(t.toString()), () -> config.get())
                .withValues(enumValue.getEnumConstants())
                .displayOnlyValue()
                .create(Component.empty(), (cbt, t) -> {});
        var wrapper = new WidgetWrapper(btn);
        wrapper.inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;");
        return createEntry_(key, wrapper, saveButton(btn::getValue, config));
    }

    private ContainerWidget createAPIKeyInput() {
        var clipboardBtn = WidgetWrapper.button(
                Component.translatable("xat.gui.config.copy_form_clipboard"),
                b -> XATConfig.LLM_API_KEY_CONFIG.set(Minecraft.getInstance().keyboardHandler.getClipboard())
        );
        clipboardBtn.inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;");

        var saveBtn = new WidgetWrapper(
                net.minecraft.client.gui.components.Button.builder(
                        Component.literal("S"),
                        _ -> XATConfig.LLM_API_KEY_CONFIG.save()
                ).bounds(0, 0, 20, 20).build()
        );
        saveBtn.inlineStyle("size: 20rpx 20rpx;");

        return createEntry_("xat.gui.config.llm_api_key", clipboardBtn, saveBtn);
    }

    private ContainerWidget createEntry_(String key, Widget input, Widget save) {
        var row = new ContainerWidget();
        row.inlineStyle("display: flex; flex-direction: row; align-items: center; height: 40rpx; width: 50%; margin-top: 8rpx; margin-left: 25%;");

        var label = new Label(IComponent.translatable(key));
        label.inlineStyle("width: 90rpx; margin-right: 16rpx; flex-shrink: 0;");
        row.addChild(label);
        row.addChild(input);
        row.addChild(save);
        return row;
    }

    private WidgetWrapper wrapInput(ObjectInputBox<?> input) {
        var wrapper = new WidgetWrapper(input);
        wrapper.setUserInput(true);
        wrapper.inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;");
        return wrapper;
    }

    private static <T> ObjectInputBox<T> createInput(java.util.function.Predicate<String> validator, java.util.function.Function<String, T> responder) {
        return new ObjectInputBox<>(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty(), validator, responder);
    }

    private static void setupEditBox(net.minecraft.client.gui.components.EditBox editBox) {
        editBox.setMaxLength(114514);
        editBox.setCanLoseFocus(true);
        editBox.scrollTo(0);
    }

    private void onDynamicUpdate() {
        if (scrollContent != null && scrollContent.tree != null) {
            rebuildProcessingBar();
            rebuildRunTransKeysRow();
            scrollContent.markDirty();
        }
    }

    @Override
    public void onClose() {
        AutoTranslate.onUpdate = () -> {};
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
