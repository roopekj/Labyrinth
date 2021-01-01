package gdx.scala.labyrinth.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import gdx.scala.labyrinth.Start;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Labyrinth";
		config.foregroundFPS = 60;
		config.backgroundFPS = 24;
		config.width = 1000;
		config.height = 1000;
		config.vSyncEnabled = true;
		config.resizable = true;
		config.forceExit = true;
		new LwjglApplication(new Start(), config);
	}
}
