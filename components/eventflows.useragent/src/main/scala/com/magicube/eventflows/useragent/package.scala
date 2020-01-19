package com.magicube.eventflows

import nl.basjes.parse.useragent.UserAgentAnalyzer

package object useragent {
  val userAgentAnalyzer = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(10000).build()
}
