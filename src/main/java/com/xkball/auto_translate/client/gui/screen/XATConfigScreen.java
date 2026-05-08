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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
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


    public XATConfigScreen(@Nullable ModContainer container, @Nullable Screen parent) {
        super(Component.empty());
        this.parentScreen = parent;
        AutoTranslate.onUpdate = this::onDynamicUpdate;

        var root = new ContainerWidget()
                .inlineStyle("size: 100% 100%; flex-direction: column;")
                .asRootStyle("""
                        * {
                            flex-shrink: 0;
                        }
                        Label {
                            text-color: -1;
                            text-height: 10rpx;
                        }
                        """)
                .addChild(createTitleBar())
                .addChild(rebuildContent())
                .addChild(createBottomBar());

        this.addScreenLayer(root);
    }

    @Override
    protected void init() {
        super.init();
    }
    
    private Widget rebuildContent(){
        if(scrollContent == null){
            scrollContent = new ContainerWidget()
                    .inlineStyle("""
                        size: 100% 100%-36rpx;
                        flex-direction: column;
                        overflow-y: scroll;
                        scrollbar-width: 8;
                        """);
        }
        else {
            scrollContent.clearChildren();
        }
        scrollContent
                .addChild(createConfigTitle("xat.gui.config.title.network"))
                .addChild(createEntry("xat.gui.config.http_host", XATConfig.HTTP_PROXY_HOST_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)))
                .addChild(createEntry("xat.gui.config.http_port", XATConfig.HTTP_PROXY_PORT_CONFIG, () -> createInput(ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER)))
                .addChild(createEntry("xat.gui.config.max_retries", XATConfig.MAX_RETRIES_CONFIG, () -> createInput(ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER)))
                .addChild(createConfigTitle("xat.gui.config.title.translator"))
                .addChild(createEntryEnum("xat.gui.config.translator", XATConfig.TRANSLATOR_TYPE_CONFIG, TranslatorType.class))
                .addChild(createTargetEntry());
        if(XATConfig.TRANSLATOR_TYPE_CONFIG.get() == TranslatorType.LLM){
            scrollContent
                    .addChild(createConfigTitle("xat.gui.config.title.llm_config"))
                    .addChild(createEntry("xat.gui.config.llm_api_url", XATConfig.LLM_API_URL_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)))
                    .addChild(createEntry("xat.gui.config.llm_model", XATConfig.LLM_MODEL_CONFIG, () -> createInput(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER)))
                    .addChild(createAPIKeyInput());
        }
        scrollContent
                .addChild(createConfigTitle("xat.gui.run_trans_keys"))
                .addChild(addNotice("xat.gui.config.run_trans_notice"))
                .addChild(createRunTransKeys())
                .addChild(createProcessingBar())
                .addChild(createConfigTitle("xat.gui.config.title.others"))
                .addChild(createTokenCostLabel())
                .addChild(createClearAllButton());
        return scrollContent;
    }

    private ContainerWidget createTitleBar() {
        var title = (Label) new Label(IComponent.translatable("xat.gui.config"))
                .inlineStyle("size: content 20rpx; text-align: center; text-scale: expand-width; margin-left: 10rpx;");

        var openConfigBtn = new WidgetWrapper(
                Button.builder(
                                Component.translatable("xat.gui.open_config_file"),
                                _ -> {
                                    try {
                                        var file = FMLPaths.CONFIGDIR.get().resolve("xkball_s_auto_translate-common.toml").toFile();
                                        Util.getPlatform().openFile(file);
                                    } catch (Exception ignored) {}
                                })
                        .bounds(0, 0, 20, 20).build())
                .inlineStyle("size: 10% 20rpx; min-width: 20rpx;margin-left: auto; margin-right: 10rpx;");

        return new ContainerWidget()
                .inlineStyle("""
                        size: 100% 24rpx;
                        display: flex;
                        flex-direction: row;
                        align-items: center;
                        flex-shrink: 0;
                        justify-content: center;
                        background-color: 0xaa222222;
                        """)
                .addChild(title)
                .addChild(openConfigBtn);
    }

    private ContainerWidget createBottomBar() {
        return new ContainerWidget()
                .inlineStyle("size: 100% 16rpx; flex-shrink: 0; background-color: 0xaa222222;");
    }

    private Label createConfigTitle(String key) {
        return (Label) new Label(IComponent.translatable(key))
                .inlineStyle("margin-top: 8rpx; margin-bottom: 8rpx; margin-left: 20%; height: 10rpx;");
    }

    private Label addNotice(String key) {
        return (Label) new Label(IComponent.translatable(key))
                .inlineStyle("margin-top: 4rpx; margin-bottom: 4rpx; margin-left: 25%; height: 10rpx");
    }

    private Label createTokenCostLabel() {
        return (Label) new Label(IComponent.translatable("xat.gui.token_cost", XATDataBase.INSTANCE.getTokenCost()))
                .inlineStyle("margin-top: 4rpx; margin-left: 25%;");
    }

    private WidgetWrapper createClearAllButton() {
        return WidgetWrapper.button(Component.translatable("xat.gui.btn.clear_all_cache"), b -> XATDataBase.INSTANCE.clearAllTranslateCache())
                .inlineStyle("height: 20rpx; margin-top: 4rpx; margin-left: 25%; width: 50%;");
    }

    private ContainerWidget createProcessingBar() {
        processingBarRow = new ContainerWidget()
                .inlineStyle("width: 50%; margin-left: 25%; flex-direction: row; align-items: center; height: 10rpx;");
        rebuildProcessingBar();
        return processingBarRow;
    }

    private void rebuildProcessingBar() {
        processingBarRow.clearChildren();
        processingBarRow
                .addChild(new Label(IComponent.literal(I18n.get("xat.gui.processing") + translateUnit.normalFinishedSize() + "/" + translateUnit.size()))
                        .inlineStyle("size: 40% auto; margin-right: 4rpx;"))
                .addChild(new Label(IComponent.literal(I18n.get("xat.gui.error") + translateUnit.errorSize() + "/" + translateUnit.size()))
                        .inlineStyle("size: 40% auto; margin-left: 4rpx;"));
    }

    private ContainerWidget createRunTransKeys() {
        runTransKeysRow = new ContainerWidget()
                .inlineStyle("width: 50%; margin-left: 25%; flex-direction: row; align-items: center; justify-content: space-around; height: 40rpx;");
        rebuildRunTransKeysRow();
        return runTransKeysRow;
    }

    private void rebuildRunTransKeysRow() {
        runTransKeysRow.clearChildren();
        var btn1 = WidgetWrapper.button(Component.translatable("xat.gui.btn.run_trans_keys"), b -> {
            if (translateUnit.finished) runTransKeys();
        }).inlineStyle("size: 30%    20rpx;");
        btn1.enabled = translateUnit.finished;

        var btn2 = WidgetWrapper.button(Component.translatable("xat.gui.btn.cancel_inject_lang"), b -> {
            translateUnit.cancel();
            XATDataBase.INSTANCE.enableInjectLang(false);
            AutoTranslate.cancelInjectLanguage();
            onDynamicUpdate();
        }).inlineStyle("size: 30% 20rpx;");
        btn2.enabled = XATDataBase.INSTANCE.isEnableInjectLang();

        runTransKeysRow
                .addChild(btn1)
                .addChild(btn2)
                .addChild(WidgetWrapper.button(Component.translatable("xat.gui.btn.clear_cache"), b -> LangKeyTranslateUnit.I18N_KEYS.clear())
                        .inlineStyle("size: 30% 20rpx;"));
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
        return new WidgetWrapper(SpriteIconButton.builder(Component.empty(), _ -> {
                    var t = supplier.get();
                    if (t != null) {
                        config.set(t);
                        config.save();
                    }
                }, true)
                .sprite(VanillaUtils.modRL("icon/save"), 16, 16)
                .build())
                .inlineStyle("size: 20rpx 20rpx;");
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

        var syncButton = new WidgetWrapper(
                Button.builder(
                        Component.literal("Sync"),
                        _ -> input.setValue(Minecraft.getInstance().getLanguageManager().getSelected()))
                        .bounds(0, 0, 20, 20).build())
                .inlineStyle("size: 30rpx 20rpx;");

        var panel = new ContainerWidget()
                .inlineStyle("align-items: center; flex-grow: 1; margin-right: 8rpx;")
                .addChild(wrapInput(input))
                .addChild(syncButton);

        return createEntry_("xat.gui.config.target", panel, saveButton(input::get, config));
    }

    private <T extends Enum<T>> ContainerWidget createEntryEnum(String key, ModConfigSpec.ConfigValue<T> config, Class<T> enumValue) {
        var btn = new CycleButton.Builder<>((t) -> Component.literal(t.toString()), config)
                .withValues(enumValue.getEnumConstants())
                .displayOnlyValue()
                .create(0,0,0,0,Component.empty(),(_,t) -> {
                    config.set(t);
                    scrollContent.submitTreeUpdate(this::rebuildContent);
                });
        return createEntry_(key,
                new WidgetWrapper(btn)
                        .inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;"),
                saveButton(btn::getValue, config));
    }

    private ContainerWidget createAPIKeyInput() {
        return createEntry_("xat.gui.config.llm_api_key",
                WidgetWrapper.button(
                                Component.translatable("xat.gui.config.copy_form_clipboard"),
                                _ -> XATConfig.LLM_API_KEY_CONFIG.set(Minecraft.getInstance().keyboardHandler.getClipboard()))
                        .inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;"),
                new WidgetWrapper(SpriteIconButton.builder(Component.empty(),
                                _ -> XATConfig.LLM_API_KEY_CONFIG.save(),true)
                                .sprite(VanillaUtils.modRL("icon/save"), 16, 16)
                                .build())
                        .inlineStyle("size: 20rpx 20rpx;"));
    }

    private ContainerWidget createEntry_(String key, Widget input, Widget save) {
        return new ContainerWidget()
                .inlineStyle("display: flex; flex-direction: row; align-items: center; height: 40rpx; width: 50%; margin-top: 4rpx; margin-left: 25%;")
                .addChild(new Label(IComponent.translatable(key))
                        .inlineStyle("width: 90rpx; margin-right: 16rpx; flex-shrink: 0;"))
                .addChild(input)
                .addChild(save);
    }

    private WidgetWrapper wrapInput(ObjectInputBox<?> input) {
        var wrapper = new WidgetWrapper(input)
                .inlineStyle("flex-grow: 1; height: 20rpx; margin-right: 8rpx;");
        wrapper.setUserInput(true);
        return wrapper;
    }

    private static <T> ObjectInputBox<T> createInput(Predicate<String> validator, Function<String, T> responder) {
        return new ObjectInputBox<>(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty(), validator, responder);
    }

    private static void setupEditBox(EditBox editBox) {
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
