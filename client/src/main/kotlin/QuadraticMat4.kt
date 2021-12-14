import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLUniformLocation
import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec4
import vision.gears.webglmath.UniformFloat
import vision.gears.webglmath.UniformProvider
import kotlin.reflect.KProperty

class QuadraticMat4(val m : Mat4 = Mat4()) : UniformFloat by m {

  constructor(vararg elements : Float) : this(Mat4(*elements))
  constructor(other : QuadraticMat4) : this(Mat4(other.m))

  fun set(other : QuadraticMat4) : QuadraticMat4{
    m.set(other.m)
    return this
  }

  fun transform(trafo : Mat4) : QuadraticMat4 {
    val trafoInv = trafo.clone().invert();
    val trafoInvT = trafoInv.clone().transpose();
    m.set( trafoInv * m * trafoInvT)
    return this
  }

  fun scale(sx : Float = 1.0f, sy : Float = 1.0f, sz : Float = 1.0f) : QuadraticMat4 {
    return transform(Mat4().scale(sx, sy, sz))
  }

  fun rotate(angle : Float = 0.0f, axisX : Float = 0.0f, axisY : Float = 0.0f, axisZ : Float = 0.0f): QuadraticMat4 {
    return transform(Mat4().rotate(angle, axisX, axisY, axisZ))
  }

  fun translate(x : Float = 0.0f, y : Float = 0.0f, z : Float = 0.0f) : QuadraticMat4{
    return transform(Mat4().translate(x, y, z))
  }

  operator fun provideDelegate(
      provider: UniformProvider,
      property: KProperty<*>) : QuadraticMat4 {
    provider.register(property.name, m)
    return this
  }

  operator fun getValue(provider: UniformProvider, property: KProperty<*>): QuadraticMat4 {
    return this
  }

  operator fun setValue(provider: UniformProvider, property: KProperty<*>, value: QuadraticMat4) {
    set(value)
  }

}

