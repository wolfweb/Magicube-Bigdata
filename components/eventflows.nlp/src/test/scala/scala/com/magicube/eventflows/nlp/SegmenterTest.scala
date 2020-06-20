package com.magicube.eventflows.nlp

import java.io.{File, PrintWriter}
import java.util.Base64

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary
import com.hankcs.hanlp.seg.NShort.NShortSegment
import com.hankcs.hanlp.seg.common.Term
import com.magicube.eventflows.Net.Curl
import com.magicube.eventflows._
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import org.junit.Test

import scala.io.Source

class SegmenterTest {
  val CustomAppendFile = "data/dictionary/custom/customAppend.txt"
  val Root = Thread.currentThread.getContextClassLoader.getResource("").getPath

  @Test
  def func_segment_cut_test(): Unit = {
    val file = "D:\\corpus.txt"
    val output = "D:\\Source\\Word2Vec.Net\\Work2VecConsoleApp\\bin\\Release\\train.txt"
    val segment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true)
    val lines = Source.fromFile(file).getLines()

    val writer = new PrintWriter(new File(output))
    for (line <- lines) {
      val terms = segment.seg(line)

      val str = terms.toArray.filter(x => !CoreStopWordDictionary.contains(x.asInstanceOf[Term].word)).map(x => x.asInstanceOf[Term].word).mkString(" ")
      writer.write(str)
      writer.write('\n')
      writer.flush()
    }
    writer.close()
  }
}
