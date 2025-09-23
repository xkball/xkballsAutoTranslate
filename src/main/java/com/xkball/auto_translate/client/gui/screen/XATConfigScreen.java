package com.xkball.auto_translate.client.gui.screen;

import com.xkball.auto_translate.AutoTranslate;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.client.gui.frame.core.HorizontalAlign;
import com.xkball.auto_translate.client.gui.frame.core.IUpdateMarker;
import com.xkball.auto_translate.client.gui.frame.core.PanelConfig;
import com.xkball.auto_translate.client.gui.frame.core.VerticalAlign;
import com.xkball.auto_translate.client.gui.frame.screen.FrameScreen;
import com.xkball.auto_translate.client.gui.frame.widget.Label;
import com.xkball.auto_translate.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.auto_translate.client.gui.frame.widget.basic.BaseContainerWidget;
import com.xkball.auto_translate.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.auto_translate.client.gui.frame.widget.basic.ScrollableVerticalPanel;
import com.xkball.auto_translate.client.gui.widget.ObjectInputBox;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.ClientUtils;
import com.xkball.auto_translate.utils.translate.LangKeyTranslateUnit;
import com.xkball.auto_translate.utils.translate.TranslatorType;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XATConfigScreen extends FrameScreen {
    
    private static final PanelConfig LINE_BASE = PanelConfig.of(0.5f, 1)
            .align(HorizontalAlign.LEFT, VerticalAlign.CENTER)
            .paddingTop(8)
            .paddingLeft(0.25f);
    private final Screen parentScreen;
    private final LangKeyTranslateUnit translateUnit = new LangKeyTranslateUnit();
    
    public XATConfigScreen(@Nullable ModContainer container, Screen parent) {
        super(Component.empty());
        this.parentScreen = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        var content = PanelConfig.of(1, 1f)
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(new ScrollableVerticalPanel()
                        .addWidget(createConfigTitle("xat.gui.config.title.network"))
                        .addWidget(createEntry("xat.gui.config.http_host", XATConfig.HTTP_PROXY_HOST_CONFIG,ObjectInputBox::createStringInput))
                        .addWidget(createEntry("xat.gui.config.http_port", XATConfig.HTTP_PROXY_PORT_CONFIG,() -> new ObjectInputBox<>(ObjectInputBox.INT_VALIDATOR,ObjectInputBox.INT_RESPONDER)))
                        .addWidget(createEntry("xat.gui.config.max_retries",XATConfig.MAX_RETRIES_CONFIG, () -> new ObjectInputBox<>(ObjectInputBox.INT_VALIDATOR,ObjectInputBox.INT_RESPONDER)))
                        .addWidget(createConfigTitle("xat.gui.config.title.translator"))
                        .addWidget(createEntryEnum("xat.gui.config.translator",XATConfig.TRANSLATOR_TYPE_CONFIG, TranslatorType.class))
                        .addWidget(createTargetEntry())
                        .addWidget(createConfigTitle("xat.gui.config.title.llm_config"))
                        .addWidget(createEntry("xat.gui.config.llm_api_url",XATConfig.LLM_API_URL_CONFIG,ObjectInputBox::createStringInput))
                        .addWidget(createEntry("xat.gui.config.llm_model",XATConfig.LLM_MODEL_CONFIG,ObjectInputBox::createStringInput))
                        .addWidget(createAPIKeyInput())
                        .addWidget(createConfigTitle("xat.gui.run_trans_keys"))
                        .addWidget(addNotice("xat.gui.config.run_trans_notice"))
                        .addWidget(createRunTransKeys())
                        .addWidget(createProcessingBar())
                        .addWidget(createConfigTitle("xat.gui.config.title.others"))
                        .addWidget(createTokenCostLabel())
                        
                );
        var screen = this.screenFrame("xat.gui.config", content);
        screen.resize();
        this.addRenderableWidget(screen);
        this.updateScreen();
    }
    
    public Label createConfigTitle(String key){
        return PanelConfig.of(1,1)
                .paddingTop(12)
                .paddingLeft(0.2f)
                .trim()
                .apply(Label.ofKey(key,1.6f));
    }
    
    public BaseContainerWidget addNotice(String key){
        return LINE_BASE.fork()
                .fixHeight(10)
                .apply(new HorizontalPanel()
                        .addWidget(PanelConfig.of(1,1)
                                .trim()
                                .apply(Label.ofKey(key))));
    }
    
    public BaseContainerWidget createTokenCostLabel(){
        return LINE_BASE.fork()
                .fixHeight(10)
                .apply(new HorizontalPanel(){
                    @Override
                    public boolean update(IUpdateMarker updateMarker) {
                        this.clearWidget();
                        this.addWidget(PanelConfig.of(1,1)
                                .trim()
                                .apply(Label.of(Component.translatable("xat.gui.token_cost",XATDataBase.INSTANCE.getTokenCost()))));
                        return true;
                    }
                });
    }
    
    public BaseContainerWidget createProcessingBar(){
        return LINE_BASE.fork()
                .align(HorizontalAlign.CENTER, VerticalAlign.CENTER)
                .fixHeight(10)
                .apply(new HorizontalPanel(){
                    @Override
                    public boolean update(IUpdateMarker updateMarker) {
                        this.clearWidget();
                        this.addWidget(PanelConfig.of(0.4f,1)
                                .paddingRight(4)
                                .trim()
                                .apply(Label.of(I18n.get("xat.gui.processing")+translateUnit.normalFinishedSize()+"/"+ translateUnit.size()).setColor(VanillaUtils.getColor(0,255,0,255))));
                        this.addWidget(PanelConfig.of(0.4f,1)
                                .paddingLeft(4)
                                .trim()
                                .apply(Label.of(I18n.get("xat.gui.error")+translateUnit.errorSize()+"/"+ translateUnit.size()).setColor(VanillaUtils.getColor(255,0,0,255))));
                        return true;
                    }
                });
    }
    
    public BaseContainerWidget createRunTransKeys(){
        var btn1 = FrameScreen.createButton("xat.gui.btn.run_trans_keys", this::runTransKeys);
        var btn2 = FrameScreen.createButton("xat.gui.btn.cancel_inject_lang", () -> {
            this.translateUnit.cancel();
            XATDataBase.INSTANCE.enableInjectLang(false);
            AutoTranslate.cancelInjectLanguage();
            XATConfigScreen.this.setNeedUpdate();
        });
        var btn3 = FrameScreen.createButton("xat.gui.btn.clear_cache", LangKeyTranslateUnit.I18N_KEYS::clear);
        var btnConfig = PanelConfig.of(0.25f,1)
                .paddingLeft(4)
                .paddingRight(4)
                .fixHeight(20);
        return LINE_BASE.fork()
                .align(HorizontalAlign.CENTER, VerticalAlign.CENTER)
                .fixHeight(40)
                .apply(new HorizontalPanel(){
                    @Override
                    public boolean update(IUpdateMarker updateMarker) {
                        btn1.inner.active = translateUnit.finished;
                        btn2.inner.active = XATDataBase.INSTANCE.isEnableInjectLang();
                        this.clearWidget();
                        this.addWidget(btnConfig.apply(btn1));
                        this.addWidget(btnConfig.apply(btn2));
                        this.addWidget(btnConfig.apply(btn3));
                        return true;
                    }
                });
    }
    
    public void runTransKeys(){
        this.translateUnit.reset();
        XATConfigScreen.this.setNeedUpdate();
        XATDataBase.INSTANCE.enableInjectLang(true);
        var en = ClientUtils.getClientLanguage("en_us");
        var target = ClientUtils.getClientLanguage(XATConfig.TARGET_LANGUAGE_CONFIG.get()).getLanguageData().keySet();
        var diff = en.getLanguageData().entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty()
                        && LangKeyTranslateUnit.I18N_KEYS.get(entry.getKey()) == null
                        && !target.contains(entry.getKey())
                ).toList();
        if(diff.isEmpty()){
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
    
    public <T> AutoResizeWidgetWrapper saveButton(Supplier<T> supplier, ModConfigSpec.ConfigValue<T> config){
        return iconButton((btn) -> {
            var t = supplier.get();
            if(t == null) return;
            config.set(t);
            config.save();
        }, VanillaUtils.modRL("icon/save"));
    }
    
    public <T> BaseContainerWidget createEntry(String key, ModConfigSpec.ConfigValue<T> config, Supplier<ObjectInputBox<T>> inputBSupplier){
        var input = inputBSupplier.get();
        FrameScreen.setupSimpleEditBox(input);
        input.setValue(config.get().toString());
        input.scrollTo(0);
        return createEntry_(key,AutoResizeWidgetWrapper.of(input),saveButton(input::get,config));
    }
    
    public BaseContainerWidget createTargetEntry(){
        var input = ObjectInputBox.createStringInput();
        var config = XATConfig.TARGET_LANGUAGE_CONFIG;
        FrameScreen.setupSimpleEditBox(input);
        input.setValue(config.get());
        input.scrollTo(0);
        var syncButton = FrameScreen.iconButton(btn -> input.setValue(Minecraft.getInstance().getLanguageManager().getSelected()),VanillaUtils.modRL("icon/sync"));
        var panel = new HorizontalPanel()
                .addWidget(PanelConfig.of(-28,1)
                        .paddingRight(8)
                        .apply(AutoResizeWidgetWrapper.of(input)))
                .addWidget(PanelConfig.ofFixSize(20,20)
                        .tooltip("xat.gui.config.tooltip.sync")
                        .apply(syncButton));
        PanelConfig.of(1,1f).align(HorizontalAlign.CENTER, VerticalAlign.CENTER).apply(panel);
        return createEntry_("xat.gui.config.target",panel,saveButton(input::get,config));
    }
    
    public <T extends Enum<T>> BaseContainerWidget createEntryEnum(String key, ModConfigSpec.ConfigValue<T> config, Class<T> enumValue){
        var btn = new CycleButton.Builder<T>((t) -> Component.literal(t.toString()))
                .withValues(enumValue.getEnumConstants())
                .withInitialValue(config.get())
                .displayOnlyValue()
                .create(Component.empty(),(cbt,t) -> {});
        return createEntry_(key,AutoResizeWidgetWrapper.of(btn),saveButton(btn::getValue,config));
    }
    
    public BaseContainerWidget createAPIKeyInput(){
        return createEntry_("xat.gui.config.llm_api_key",
                createButton("xat.gui.config.copy_form_clipboard",() -> XATConfig.LLM_API_KEY_CONFIG.set(Minecraft.getInstance().keyboardHandler.getClipboard())),
                iconButton((bt) -> XATConfig.LLM_API_KEY_CONFIG.save(), VanillaUtils.modRL("icon/save")));
    }
    
    private BaseContainerWidget createEntry_(String key, BaseContainerWidget input, AutoResizeWidgetWrapper save){
        return LINE_BASE.fork()
                .fixHeight(40)
                .apply(new HorizontalPanel()
                        .addWidget(PanelConfig.of(1,1)
                                .fixWidth(90)
                                .paddingRight(16)
                                .trim()
                                .apply(Label.ofKey(key,1.2f)))
                        .addWidget(PanelConfig.of(0.6f,1)
                                .fixHeight(20)
                                .paddingRight(8)
                                .apply(input)))
                        .addWidget(PanelConfig.ofFixSize(20,20)
                                .tooltip("xat.gui.config.save")
                                .apply(save));
    }
    
    
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
