package at.anchor.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import at.anchor.game.EsthetiqueGems;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		// config.title = "Esthetique GEms";
		config.useGL30 = true;
		config.width = 460;
		config.height = 820;

		new LwjglApplication(new EsthetiqueGems(), config);
	}
}
