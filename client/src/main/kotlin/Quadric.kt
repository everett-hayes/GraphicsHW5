import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec4
import vision.gears.webglmath.UniformProvider

class Quadric(
  id : Int,
  vararg programs : Program) : UniformProvider("quadrics[$id]") {

  val surface by Mat4();
  val clipper1 by Mat4();
  val clipper2 by Mat4();
  val clipper3 by Mat4();
  val clipper4 by Mat4();
  val kd by Vec4();
  val kr by Vec4();
  
  init{
    addComponentsAndGatherUniforms(*programs)
  }

  companion object {
  }
}
