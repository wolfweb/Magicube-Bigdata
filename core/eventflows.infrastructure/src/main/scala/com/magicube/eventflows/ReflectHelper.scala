package com.magicube.eventflows

object ReflectHelper {
  import scala.reflect.runtime.universe._
  val runtime = runtimeMirror(getClass.getClassLoader)

  def classAnnotation[T: TypeTag, A: TypeTag]() = {
    val tpe = typeOf[T]
    val annotTag = implicitly[WeakTypeTag[A]]
    val res = tpe.typeSymbol.annotations.find(ann => {
      ann.tree.tpe =:= annotTag.tpe
    })
    res match{
      case None => None
      case _ => Some(parseAnnotation[A](res.get))
    }
  }

  def memberAnnotation[T: TypeTag, A: WeakTypeTag](name: String) = {
    val tpe = typeOf[T]
    val annotTag = implicitly[WeakTypeTag[A]]
    val member = tpe.decl(TermName(name))
    var res: Option[Annotation] = null
    if (member != null) {
      res = member.annotations.find(x => x.tree.tpe =:= annotTag.tpe)
    }
    if (res == None) {
      val constructor = tpe.decls.filter(d => d.isMethod && d.asMethod.isPrimaryConstructor).head
      val params = constructor.typeSignature.paramLists.head
      val item = params.find(x => x.name.toString.trim == name)
      res = item match {
        case None => None
        case _ => item.get.annotations.find(x => x.tree.tpe =:= annotTag.tpe)
      }
    }
    res match{
      case None => None
      case _ => Some(parseAnnotation[A](res.get))
    }
  }

  private def parseAnnotation[A: WeakTypeTag](annotation: Annotation) = {
    val annotTag = implicitly[WeakTypeTag[A]]
    val tpe = annotTag.tpe
    val value = annotation.tree.children.tail.map(_.productElement(0).asInstanceOf[Constant].value)
    runtime.reflectClass(tpe.typeSymbol.asClass).reflectConstructor(tpe.decl(termNames.CONSTRUCTOR).asMethod)(value: _*).asInstanceOf[A]
  }
}
