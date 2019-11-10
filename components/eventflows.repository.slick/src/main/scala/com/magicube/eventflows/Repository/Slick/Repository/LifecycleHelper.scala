package com.magicube.eventflows.Repository.Slick.Repository

import java.util.concurrent.ConcurrentHashMap

import com.magicube.eventflows.Repository.Slick.Exception._

import scala.annotation.StaticAnnotation
import scala.collection.concurrent.Map
import scala.collection.convert.decorateAsScala._

class LifecycleHelper{
  private[Repository] val lifecycleHandlerCache: Map[LifecycleHandlerCacheKey, Boolean] = new ConcurrentHashMap[LifecycleHandlerCacheKey, Boolean]().asScala

  def isLifecycleHandlerDefined(clazz: Class[_ <: BaseRepository[_, _]], handlerType: Class[_ <: StaticAnnotation]): Boolean = {
    lifecycleHandlerCache.get(
      LifecycleHandlerCacheKey(clazz, handlerType)
    ).isDefined
  }

  def createLifecycleHandler[T <: Entity[T, Id], Id](repo: BaseRepository[T, Id], handlerType: Class[_ <: StaticAnnotation]): (T => T) = {
    val baseRepositoryClass = classOf[BaseRepository[_, _]]
    var methods: Seq[(Type, String)] = Seq()
    var repositoryClass: Class[_] = repo.getClass
    val mirror = runtimeMirror(repositoryClass.getClassLoader)

    while (baseRepositoryClass.isAssignableFrom(repositoryClass)) {
      methods = getHandlerMethods(mirror, repositoryClass, handlerType) ++: methods
      repositoryClass = repositoryClass.getSuperclass
    }

    val handlers = createHandlers[T, Id](mirror, repo, methods)
    if (handlers.nonEmpty) {
      lifecycleHandlerCache.put(LifecycleHandlerCacheKey(repo.getClass, handlerType), true)
    }
    createHandlerChain(handlers, handlerType)
  }

  private def createHandlerChain[T](handlers: Seq[(Type, MethodMirror)], handlerType: Class[_ <: StaticAnnotation]): T => T = {
    x =>
      handlers.foldLeft(x)((acc: T, m: (Type, MethodMirror)) => invokeHandler(acc, m, handlerType))
  }

  private def invokeHandler[T](acc: T, m: (Type, MethodMirror), handlerType: Class[_ <: StaticAnnotation]): T = {
    try {
      m._2(acc).asInstanceOf[T]
    } catch {
      case e @ (_ : IllegalArgumentException | _ : ArrayIndexOutOfBoundsException) =>
        throw new ListenerInvocationException("Error while invoking listener for event type " + handlerType.getSimpleName + " in class " + m._1.baseClasses.head.fullName + ". Confirm that the handler method accepts a single parameter which type is compatible with the repository entity type", e)
    }
  }

  private def createHandlers[T <: Entity[T, Id], Id](mirror: Mirror, repo: BaseRepository[T, Id], methods: Seq[(Type, String)]) = {
    val repoMirror = mirror.reflect(repo)
    methods.map(
      m => (
        m._1,
        repoMirror.reflectMethod(
          m._1.member(TermName(m._2)).asMethod
        )
      )
    )
  }

  private def getHandlerMethods[R <: BaseRepository[_, _]](mirror: Mirror, repositoryClass: Class[_], handlerType: Class[_ <: StaticAnnotation]): Seq[(Type, String)] = {
    validateHandlerMethods(
      repositoryClass,
      handlerType,
      repositoryClass.getDeclaredMethods
        .map(m => (mirror.staticClass(repositoryClass.getName).selfType, m.getName))
        .filter(m => isEventHandler(m._1, m._2, handlerType))
    )
  }

  private def isEventHandler(repoType: Type, method: String, handlerType: Class[_ <: StaticAnnotation]): Boolean = {
    repoType.member(TermName(method)) match {
      case m if !m.isMethod => false
      case m =>
        m.asMethod.annotations
          .map(a => a.tree.tpe.baseClasses.head.fullName)
          .contains(handlerType.getName)
    }
  }

  private def validateHandlerMethods(repositoryClass: Class[_], handlerType: Class[_ <: StaticAnnotation], methods: Seq[(Type, String)]): Seq[(Type, String)] = {
    if (methods.size > 1) {
      throw new DuplicatedHandlerException(
        "Only a single event handler for a given event type is allowed in the same repository class. " +
          "Repository class: " + repositoryClass.getSimpleName + ", " +
          "eventType: " + handlerType.getSimpleName
      )
    }
    methods
  }
}

object LifecycleHelper extends LifecycleHelper

private case class LifecycleHandlerCacheKey(clazz: Class[_ <: BaseRepository[_, _]], handlerType: Class[_ <: StaticAnnotation])