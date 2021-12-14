import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Vec4
import vision.gears.webglmath.Mat4
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos

class Scene (
  val gl : WebGL2RenderingContext)  : UniformProvider("scene") {

  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fsTrace = Shader(gl, GL.FRAGMENT_SHADER, "trace-fs.glsl")  
  val traceProgram = Program(gl, vsQuad, fsTrace)
  val quadGeometry = TexturedQuadGeometry(gl)  

  val traceMaterial = Material(traceProgram).apply{
    this["envTexture"]?.set(TextureCube(gl, 
      "media/fall21_posx.png",
      "media/fall21_negx.png",
      "media/fall21_posy.png",
      "media/fall21_negy.png",
      "media/fall21_posz.png",
      "media/fall21_negz.png"))
  }

  val traceQuad = Mesh(traceMaterial, quadGeometry)

  val camera = PerspectiveCamera(*Program.all).apply{
    update()
  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat()/canvas.height)
  }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  val quadrics = Array<Quadric>(2) { Quadric(it, *Program.all) }

  val sphere = Mat4(1.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 1.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 1.0f, 0.0f,             
                   0.0f, 0.0f, 0.0f, -0.2f)

  val board = Mat4(0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 1.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f,
                   0.0f, 0.0f, 0.0f, 0.0f)

  // cuts horizontally close to me
  val boardCut1 = Mat4(0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 1.0f,             
                   0.0f, 0.0f, 0.0f, 0.0f)

  // cuts vertically to the left
  val boardCut2 = Mat4(0.0f, 0.0f, 0.0f, 1.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f,             
                   0.0f, 0.0f, 0.0f, -2.0f)

  // cuts vertically to the right
  val boardCut3 = Mat4(0.0f, 0.0f, 0.0f, 1.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f,             
                   0.0f, 0.0f, 0.0f, -21.0f)

  // cuts horizontally far from me
  val boardCut4 = Mat4(0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 1.0f,             
                   0.0f, 0.0f, 0.0f, 20.0f)

  val noClipper = Mat4(0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f, 
                   0.0f, 0.0f, 0.0f, 0.0f,             
                   0.0f, 0.0f, 0.0f, -1.0f)

  init {
    quadrics[0].surface.set(board)    
    quadrics[0].surface.translate(5f)
    quadrics[0].clipper1.set(boardCut1)
    quadrics[0].clipper2.set(boardCut2)
    quadrics[0].clipper3.set(boardCut3)
    quadrics[0].clipper4.set(boardCut4)   
    quadrics[0].kd.set(1.0f, 0.0f, 0.0f)
    quadrics[0].kr.set(0.3f, 0.3f, 0.0f)

    quadrics[1].surface.set(sphere)
    quadrics[1].surface.translate(50f, 50f, 40f);
    quadrics[1].clipper1.set(boardCut1)
    quadrics[1].clipper2.set(boardCut2)
    quadrics[1].clipper3.set(boardCut3)
    quadrics[1].clipper4.set(boardCut4)
    quadrics[1].kd.set(0.0f, 0.0f, 1.0f)
    quadrics[1].kr.set(0.3f, 0.3f, 0.0f)

    addComponentsAndGatherUniforms(*Program.all)
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f
    timeAtLastFrame = timeAtThisFrame

    camera.move(dt, keysPressed) 
    
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    traceQuad.draw(this, camera, *quadrics);
  }
}
