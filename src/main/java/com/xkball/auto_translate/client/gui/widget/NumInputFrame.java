package com.xkball.auto_translate.client.gui.widget;

import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.ui.widget.mc.ObjectInputBox;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class NumInputFrame<T extends Number> extends ContainerWidget {

    @Nullable
    private Consumer<T> valueSetter;

    public NumInputFrame() {
        this.inlineStyle("display: flex; flex-direction: row; align-items: center;");
    }

    protected void addDecreaseButton() {
        this.addChild(WidgetWrapper.button("-", btn -> this.onDecreaseButtonClick())
                .inlineStyle("size: 20rpx 20rpx; margin-right: 8rpx;"));
    }

    protected void addIncreaseButton() {
        this.addChild(WidgetWrapper.button("+", btn -> this.onIncreaseButtonClick())
                .inlineStyle("size: 20rpx 20rpx; margin-left: 8rpx;"));
    }

    protected void addNumLabel() {
        var label = new Label();
        this.addChild(label.inlineStyle("flex-grow: 1; text-align: center;"));
    }

    protected Label getNumLabel() {
        for (var child : this.children) {
            if (child instanceof Label label) return label;
        }
        throw new IllegalStateException("No num label found");
    }

    public NumInputFrame<T> setValueSetter(Consumer<T> valueSetter) {
        this.valueSetter = valueSetter;
        return this;
    }

    public void onValueChange() {
        if (valueSetter != null) {
            valueSetter.accept(getValue());
        }
    }

    public abstract void onDecreaseButtonClick();

    public abstract void onIncreaseButtonClick();

    public abstract @Nullable T getValue();

    public static class Pow2IntInput extends NumInputFrame<Integer> {
        private final int minN;
        private final int maxN;
        private int n;

        public Pow2IntInput(int minN, int maxN, int defaultN) {
            this.addDecreaseButton();
            this.addNumLabel();
            this.addIncreaseButton();
            this.minN = minN;
            this.maxN = maxN;
            this.setN(defaultN);
        }

        @Override
        public void onDecreaseButtonClick() {
            var nn = Math.max(minN, n - 1);
            this.setN_(nn);
        }

        @Override
        public void onIncreaseButtonClick() {
            var nn = Math.min(n + 1, maxN);
            this.setN_(nn);
        }

        @Override
        public @Nullable Integer getValue() {
            return (int) Math.pow(2, n);
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            var nn = Mth.clamp(n, minN, maxN);
            this.setN_(nn);
        }

        private void setN_(int nn) {
            if (nn == n) return;
            this.n = nn;
            getNumLabel().setText(String.valueOf(getValue()));
            this.onValueChange();
        }
    }

    public static class FloatInput extends NumInputFrame<Float> {

        private final float min;
        private final float max;
        private final float delta;
        @Nullable
        private Float value;
        @Nullable
        private ObjectInputBox<Float> input;

        public FloatInput(float min, float max, float delta, float default_, boolean renderNumLabel) {
            this.min = min;
            this.max = max;
            this.delta = delta;
            this.addDecreaseButton();
            if (renderNumLabel) {
                this.addNumLabel();
            } else {
                this.input = new ObjectInputBox<>(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, net.minecraft.network.chat.Component.empty(), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
                input.setResponder(str -> {
                    var val = input.get();
                    if (val != null) {
                        this.setValue(val);
                    }
                });
                var wrapper = new WidgetWrapper(input);
                wrapper.setUserInput(true);
                wrapper.inlineStyle("flex-grow: 1;");
                this.addChild(wrapper);
            }
            this.addIncreaseButton();
            this.setValue(default_);
        }

        public void setValue(@Nullable Float value) {
            if (Objects.equals(this.value, value)) return;
            value = value == null ? null : Mth.clamp(value, min, max);
            if (input != null && value != null) {
                input.setValue(String.valueOf(value));
                input.scrollTo(0);
            }
            if (value != null) {
                try {
                    getNumLabel().setText(String.valueOf(value));
                } catch (IllegalStateException ignored) {
                    // FloatInput without renderNumLabel has no label
                }
            }
            this.value = value;
            this.onValueChange();
        }

        @Override
        public void onDecreaseButtonClick() {
            if (value == null) {
                this.setValue(min);
            } else {
                this.setValue(Math.max(min, value - delta));
            }
        }

        @Override
        public void onIncreaseButtonClick() {
            if (value == null) {
                this.setValue(max);
            } else {
                this.setValue(Math.min(value + delta, max));
            }
        }

        @Override
        public @Nullable Float getValue() {
            return value;
        }
    }

}
