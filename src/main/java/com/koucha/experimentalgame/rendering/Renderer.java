package com.koucha.experimentalgame.rendering;

import com.koucha.experimentalgame.entitySystem.component.Mesh;
import com.koucha.experimentalgame.input.InputBridge;
import org.joml.Matrix4f;

/**
 * Takes care of creating a window,
 * provides functionality to render {@link Renderable} objects to this window
 * and forwards the input received by the Window to a {@link InputBridge}
 * <p>
 * A minimal setup should always have the form:
 * <blockquote><pre>
 * {@code
 * try{
 *     renderer.init();
 * <p>
 *     // create window and do your stuff here,
 *     // exit try block only if program ends
 * <p>
 * } finally{
 *     renderer.cleanUp();
 * }
 * }
 * </pre></blockquote>
 */
public interface Renderer
{
	/**
	 * Renders the given {@link Renderable}
	 *
	 * @param renderable is rendered
	 */
	void render( Mesh renderable );

	void createFullscreenWindow( String title );

	void setAlpha( float alpha );

	/**
	 * @param inputBridge receiver of the input captured by the renderer
	 */
	void setInputBridge( InputBridge inputBridge );

	/**
	 * Updates the input provided through the {@link InputBridge}
	 */
	void initializeUpdateIteration();

	/**
	 * Sets the renderer up to begin the rendering of a new frame
	 */
	void initializeRenderIteration();

	/**
	 * Finishes rendering the Frame and passes it on to be displayed
	 */
	void finishRenderIteration();

	/**
	 * @return true if the window requests to be closed (for example because the user closed it)
	 */
	boolean windowShouldClose();

	/**
	 * Sets up the renderer
	 * <p>
	 * HAS TO BE CALLED BEFORE ANYTHING ELSE!
	 */
	void init();

	/**
	 * Prepares the renderer for rendering input
	 * <p>
	 * HAS TO BE CALLED ONCE BEFORE RENDERING!
	 */
	void preLoopInitialization();

	/**
	 * Cleans up all the resources reserved by the renderer
	 * <p>
	 * Should be as soon as the renderer isn't used anymore
	 * <p>
	 * Graphics interfaces used by renderers often work outside of the garbage collector.
	 * Because of that, this method should be used to avoid memory leaks etc.
	 */
	void cleanUp();

	/**
	 * If vertical synchronization (vSync) is enabled {@link #finishRenderIteration()} will
	 * wait for the display to update before returning
	 *
	 * @return true if vertical synchronization is enabled
	 */
	boolean vSyncEnabled();

	int getWindowHeight();

	int getWindowWidth();

	/**
	 * Sets the renderer up to draw 2D Gui elements (orthographic projection)
	 */
	void initializeGUIRenderIteration();

	/**
	 * set the camera transformation used to render 3D scenes
	 *
	 * @param cameraMatrix camera matrix without perspective ({@code world -> camera space})
	 */
	void setCameraMatrix( Matrix4f cameraMatrix );
}
