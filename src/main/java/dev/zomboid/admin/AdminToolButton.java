package dev.zomboid.admin;

import zombie.core.Color;
import zombie.core.textures.Texture;
import zombie.ui.TextManager;
import zombie.ui.UIElement;
import zombie.ui.UIEventHandler;
import zombie.ui.UIFont;

public class AdminToolButton extends UIElement {
    public boolean clicked = false;
    public UIElement MessageTarget;
    public boolean mouseOver = false;
    public String name;
    public String text;
    Texture downLeft;
    Texture downMid;
    Texture downRight;
    float origX;
    Texture upLeft;
    Texture upMid;
    Texture upRight;
    private UIEventHandler MessageTarget2 = null;

    public AdminToolButton(UIElement target, float x, float y, String text, String name) {
        this.x = x;
        this.y = y;
        this.origX = x;
        this.MessageTarget = target;
        this.upLeft = Texture.getSharedTexture("ButtonL_Up");
        this.upMid = Texture.getSharedTexture("ButtonM_Up");
        this.upRight = Texture.getSharedTexture("ButtonR_Up");
        this.downLeft = Texture.getSharedTexture("ButtonL_Down");
        this.downMid = Texture.getSharedTexture("ButtonM_Down");
        this.downRight = Texture.getSharedTexture("ButtonR_Down");
        this.name = name;
        this.text = text;
        this.width = (float) TextManager.instance.MeasureStringX(UIFont.Small, text);
        this.width += 8.0F;
        if (this.width < 40.0F) {
            this.width = 40.0F;
        }

        this.height = (float) this.downMid.getHeight();
    }

    public AdminToolButton(UIEventHandler var1, int var2, int var3, String var4, String var5) {
        this.x = var2;
        this.y = var3;
        this.origX = (float) var2;
        this.MessageTarget2 = var1;
        this.upLeft = Texture.getSharedTexture("ButtonL_Up");
        this.upMid = Texture.getSharedTexture("ButtonM_Up");
        this.upRight = Texture.getSharedTexture("ButtonR_Up");
        this.downLeft = Texture.getSharedTexture("ButtonL_Down");
        this.downMid = Texture.getSharedTexture("ButtonM_Down");
        this.downRight = Texture.getSharedTexture("ButtonR_Down");
        this.name = var5;
        this.text = var4;
        this.width = (float) TextManager.instance.MeasureStringX(UIFont.Small, var4);
        this.width += 8.0F;
        if (this.width < 40.0F) {
            this.width = 40.0F;
        }

        this.height = (float) this.downMid.getHeight();
    }

    public Boolean onMouseDown(double var1, double var3) {
        if (!this.isVisible()) {
            return false;
        } else {
            this.clicked = true;
            return Boolean.TRUE;
        }
    }

    public Boolean onMouseMove(double var1, double var3) {
        this.mouseOver = true;
        return Boolean.TRUE;
    }

    public void onMouseMoveOutside(double var1, double var3) {
        this.clicked = false;
        this.mouseOver = false;
    }

    public Boolean onMouseUp(double var1, double var3) {
        if (this.clicked) {
            if (this.MessageTarget2 != null) {
                this.MessageTarget2.Selected(this.name, 0, 0);
            } else if (this.MessageTarget != null) {
                this.MessageTarget.ButtonClicked(this.name);
            }
        }

        this.clicked = false;
        return Boolean.TRUE;
    }

    public void render() {
        if (this.isVisible()) {
            boolean var1 = false;
            if (this.clicked) {
                this.DrawTexture(this.downLeft, 0.0D, 0.0D, 1.0D);
                this.DrawTextureScaledCol(this.downMid, this.downLeft.getWidth(), 0.0D, (int) (this.getWidth() - (double) (this.downLeft.getWidth() * 2)), this.downLeft.getHeight(), new Color(255, 255, 255, 255));
                this.DrawTexture(this.downRight, (int) (this.getWidth() - (double) this.downRight.getWidth()), 0.0D, 1.0D);
                this.DrawTextCentre(this.text, this.getWidth() / 2.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
            } else {
                this.DrawTexture(this.upLeft, 0.0D, 0.0D, 1.0D);
                this.DrawTextureScaledCol(this.upMid, this.downLeft.getWidth(), 0.0D, (int) (this.getWidth() - (double) (this.downLeft.getWidth() * 2)), this.downLeft.getHeight(), new Color(255, 255, 255, 255));
                this.DrawTexture(this.upRight, (int) (this.getWidth() - (double) this.downRight.getWidth()), 0.0D, 1.0D);
                this.DrawTextCentre(this.text, this.getWidth() / 2.0D, 0.0D, 1.0D, 1.0D, 1.0D, 1.0D);
            }

            super.render();
        }
    }
}
