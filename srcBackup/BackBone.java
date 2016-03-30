package com.koucha.experimentalgame;

import com.koucha.experimentalgame.entitySystem.system.Camera;
import com.koucha.experimentalgame.entitySystem.component.Entity;
import com.koucha.experimentalgame.entitySystem.system.PlayerController;
import com.koucha.experimentalgame.entitySystem.system.Position;
import com.koucha.experimentalgame.input.InputBridge;
import com.koucha.experimentalgame.input.InputEvent;
import com.koucha.experimentalgame.lwjgl.GLFWGraphicsHub;
import com.koucha.experimentalgame.rendering.Color;
import com.koucha.experimentalgame.rendering.Cube;
import com.koucha.experimentalgame.rendering.Playah;
import com.koucha.experimentalgame.rendering.Renderer;
import org.joml.Vector3f;

/**
 * Contains the game loop
 */
public class BackBone
{
	// Specify which graphics implementation should be used
	public static final GraphicsHub GRAPHICS_HUB = new GLFWGraphicsHub();

	public static final int INITIAL_WIDTH = 1200, INITIAL_HEIGHT = INITIAL_WIDTH * 9 / 12;

	public static final int INITIAL_UPDATES_PER_SECOND = 120;

	private boolean running = false;

	private Renderer renderer;
	private GameObjectList list;
	private HUD hud;
	private InputBridge inputBridge;
	@SuppressWarnings( "FieldCanBeLocal" )
	private long limitedNanosecondsPerFrame = 1000000000 / 60;
	private boolean limitFPS = false;
	private int framesPerSecond;

	/**
	 * Sets up all the basics needed for the {@link #loop()} to run
	 */
	private BackBone()
	{
		renderer = GRAPHICS_HUB.createRenderer();

		renderer.init();

		renderer.createWindow( INITIAL_WIDTH, INITIAL_HEIGHT, "Tha Game" );

		inputBridge = new InputBridge();
		inputBridge.getInputMap().addLink( "Quit", 0x100, ( InputEvent evt ) -> running = false );


		list = new GameObjectList();

		Position position = new Position();
		list.add( new Entity( position, new Cube( new Vector3f( 0.1f, 0.1f, 100f ), position, new Color( 0.5f, 0.6f, 0f ) ) ) );
		list.add( new Entity( position, new Cube( new Vector3f( 100f, 0.1f, 0.1f ), position, new Color( 0.6f, 0.5f, 0f ) ) ) );

		hud = new HUD( generatePlayer() );

		renderer.setInputBridge( inputBridge );

		// if v-sync is not done by renderer, limit the FPS manually
		limitFPS = !renderer.vSyncEnabled();

		// todo debug
//		limitFPS = false;
	}

	private Entity generatePlayer()
	{
		Camera camera = new Camera( INITIAL_WIDTH / ((float) INITIAL_HEIGHT) );
		camera.setOffset( new Vector3f( 0f, 1f, 8f ) );
		renderer.setCamera( camera );
		Position position = new Position( new Vector3f( 0f, 0.5f, 0f ) );
		PlayerController pc = new PlayerController( inputBridge, position, camera );
		Entity player = new Entity( position, new Playah( new Vector3f( 1f, 1f, 1f ), position, new Color( 0f, 0f, 1f ) ) );
		player.addComponent( pc );
		list.add( player );
		return player;
	}

	public static void main( String[] args )
	{
		BackBone backBone = new BackBone();
		try
		{
			backBone.loop();
		} finally
		{
			backBone.cleanUp();
		}
	}

	/**
	 * Main loop, all the timing is done in here
	 */
	private void loop()
	{
		running = true;

		long lastTimeNS = System.nanoTime();
		double nanosecondsPerUpdate = 1000000000d / INITIAL_UPDATES_PER_SECOND;

		/**
		 * This many updates should be done in the next iteration
		 *
		 * Is floating point! If it's increased by 0.1 in each iteration, update will only be called every 10th iteration.
		 * If dependent on the duration of the Iteration, updates will be done with a fixed frequency
		 */
		double updatesToBeDone = 0;

		long timerNS = lastTimeNS;
		long fpsTimerNS = lastTimeNS;
		long nowTimeNS;

		renderer.preLoopInitialization();

		while( running && !renderer.windowShouldClose() )
		{
			nowTimeNS = System.nanoTime();
			updatesToBeDone += (nowTimeNS - lastTimeNS) / nanosecondsPerUpdate;        // ex.: 1.4 = 0.5 + 0.9 (0.5: remainder from last iteration, 0.9: from this iteration)
			lastTimeNS = nowTimeNS;

			// update
			while( updatesToBeDone >= 1 )
			{
				update();
				updatesToBeDone--;        // ex.: 0.4 = 1.4--  (0.4: remainder from this iteration)
			}

			// render
			if( running )
			{
				// if FPS are limited to a certain value, slow the render loop to match
				if( limitFPS )
				{
					nowTimeNS = System.nanoTime();
					while( nowTimeNS - fpsTimerNS < limitedNanosecondsPerFrame )
					{
						Thread.yield();
						nowTimeNS = System.nanoTime();
					}
					fpsTimerNS += limitedNanosecondsPerFrame;
				}

				render();
				framesPerSecond++;
			}

			if( nowTimeNS - timerNS > 1000000000 )
			{
				// todo DEBUG
				//System.out.println( "FPS: " + framesPerSecond + "; Limited FPS " + ((limitFPS) ? ("on") : ("off")) );

				timerNS += 1000000000;
				framesPerSecond = 0;
			}
		}

	}

	/**
	 * Perform any cleanup that has to be done before the program ends
	 */
	private void cleanUp()
	{
		renderer.cleanUp();
	}

	/**
	 * Does all the calculations inside the game loop
	 */
	private void update()
	{
		renderer.initializeUpdateIteration();

		list.update();
		// hud.update(); // not really needed
	}

	/**
	 * Does all the rendering inside the game loop
	 */
	private void render()
	{
		renderer.initializeRenderIteration();

		list.render( renderer );

		renderer.initializeGUIRenderIteration();

		hud.render( renderer );

		renderer.finishRenderIteration();
	}
}