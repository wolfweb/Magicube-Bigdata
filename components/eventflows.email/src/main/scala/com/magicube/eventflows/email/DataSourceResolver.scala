package com.magicube.eventflows.email

import java.io.{File, IOException}
import java.net.URL

import javax.activation.{DataSource, FileDataSource, FileTypeMap, URLDataSource}
import javax.mail.util.ByteArrayDataSource

trait DataSourceResolver {
  def resolve(var1: String): DataSource

  def resolve(var1: String, var2: Boolean): DataSource
}

abstract class DataSourceBaseResolver(v: Boolean) extends DataSourceResolver {
  var lenient = v

  protected def isCid(resourceLocation: String): Boolean = resourceLocation.startsWith("cid:")

  protected def isFileUrl(urlString: String): Boolean = urlString.startsWith("file:/")

  protected def isHttpUrl(urlString: String): Boolean = urlString.startsWith("http://") || urlString.startsWith("https://")
}

class DataSourceUrlResolver(url: URL, lenient: Boolean = false) extends DataSourceBaseResolver(lenient) {
  val baseUrl: URL = url

  override def resolve(resourceLocation: String): DataSource = this.resolve(resourceLocation, lenient)

  override def resolve(resourceLocation: String, isLenient: Boolean): DataSource = {
    var result: URLDataSource = null
    try {
      if (!this.isCid(resourceLocation)) {
        val url = this.createUrl(resourceLocation)
        result = new URLDataSource(url)
        result.getInputStream
      }
      result
    } catch {
      case var5: IOException => {
        if (isLenient) null
        else throw var5
      }
    }
  }

  protected def createUrl(resourceLocation: String): URL = {
    if (this.baseUrl == null)
      new URL(resourceLocation)
    else if (resourceLocation != null && resourceLocation.length != 0)
      if (!this.isFileUrl(resourceLocation) && !this.isHttpUrl(resourceLocation))
        new URL(this.baseUrl, resourceLocation.replaceAll("&amp;", "&"))
      else
        new URL(resourceLocation)
    throw new IllegalArgumentException("No resource defined")
  }
}

class DataSourceFileResolver(file: File, lenient: Boolean = false) extends DataSourceBaseResolver(lenient) {
  val baseDir = file

  override def resolve(resourceLocation: String): DataSource = this.resolve(resourceLocation, this.lenient)

  override def resolve(resourceLocation: String, isLenient: Boolean): DataSource = {
    var result: DataSource = null
    if (!this.isCid(resourceLocation)) {
      var file = new File(resourceLocation)
      if (!file.isAbsolute) file = if (this.baseDir != null) new File(this.baseDir, resourceLocation)
      else new File(resourceLocation)
      if (file.exists) result = new FileDataSource(file)
      else if (!isLenient) throw new IOException("Cant resolve the following file resource :" + file.getAbsolutePath)
    }
    result
  }
}

class DataSourceClassPathResolver(path: String, lenient: Boolean = false) extends DataSourceBaseResolver(lenient) {
  val classPathBase : String = if (path.endsWith("/")) classPathBase else path + "/"

  override def resolve(resourceLocation: String): DataSource = this.resolve(resourceLocation, this.lenient)

  override def resolve(resourceLocation: String, isLenient: Boolean): DataSource = {
    var result: DataSource = null
    try {
      if (!this.isCid(resourceLocation) && !this.isHttpUrl(resourceLocation)) {
        val mimeType = FileTypeMap.getDefaultFileTypeMap.getContentType(resourceLocation)
        val resourceName = this.getResourceName(resourceLocation)
        val is = classOf[DataSourceClassPathResolver].getResourceAsStream(resourceName)
        if (is == null) {
          if (isLenient) return null
          throw new IOException("The following class path resource was not found : " + resourceLocation)
        }
        try {
          val ds = new ByteArrayDataSource(is, mimeType)
          ds.setName(classOf[DataSourceClassPathResolver].getResource(resourceName).toString)
          result = ds
        } finally is.close()
      }
      result
    } catch {
      case var12: IOException => {
        if (isLenient) null
        else throw var12
      }
    }
  }

  private def getResourceName(resourceLocation: String): String = {
    s"${this.classPathBase}$resourceLocation".replaceAll("//", "/")
  }
}

class DataSourceCompositeResolver(resolvers: Array[DataSourceResolver], lenient: Boolean = false) extends DataSourceBaseResolver(lenient) {
  val dataSourceResolvers = resolvers

  def getDataSourceResolvers: Array[DataSourceResolver] = {
    val resolvers = new Array[DataSourceResolver](this.dataSourceResolvers.length)
    System.arraycopy(this.dataSourceResolvers, 0, resolvers, 0, this.dataSourceResolvers.length)
    resolvers
  }

  override def resolve(resourceLocation: String): DataSource = {
    val result = this.resolve(resourceLocation, true)
    if (!this.lenient && result == null) throw new IOException("The following resource was not found : " + resourceLocation)
    else result
  }

  override def resolve(resourceLocation: String, isLenient: Boolean): DataSource = {
    for (i <- 0 until this.getDataSourceResolvers.length) {
      val dataSourceResolver = this.getDataSourceResolvers(i)
      val dataSource = dataSourceResolver.resolve(resourceLocation, isLenient)
      if (dataSource != null) return dataSource
    }
    if (isLenient) null
    else throw new IOException("The following resource was not found : " + resourceLocation)
  }
}

