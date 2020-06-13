package com.magicube.eventflows.Spring

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.{BeanDefinitionBuilder, BeanDefinitionRegistry, BeanDefinitionRegistryPostProcessor, DefaultListableBeanFactory}
import org.springframework.context.annotation.Configuration
import scala.reflect.ClassTag

@Configuration
abstract class ComponentRegistryPostProcessor[T: ClassTag]()(implicit val clasz : Class[T]) extends BeanDefinitionRegistryPostProcessor {
  //类型注入
  override def postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry): Unit = {
    if(BuildInstance==null) {
      val builder = BeanDefinitionBuilder.genericBeanDefinition(clasz)
      val define = builder.getRawBeanDefinition
      registry.registerBeanDefinition(clasz.getSimpleName, define)
    }
  }

  //实例注入
  override def postProcessBeanFactory(factory: ConfigurableListableBeanFactory): Unit = {
    val builder = BeanDefinitionBuilder.genericBeanDefinition(clasz, () => BuildInstance)
    val define = builder.getRawBeanDefinition()
    factory.asInstanceOf[DefaultListableBeanFactory].registerBeanDefinition(clasz.getSimpleName, define)
  }

  def BuildInstance: T = null.asInstanceOf[T]
}
