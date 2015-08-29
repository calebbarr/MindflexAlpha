package com.xbarr.mindflex

import java.net.{Socket,InetAddress}
import scala.io.{BufferedSource,Source}
import Constants._
import java.util.zip.GZIPInputStream
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{S3ObjectSummary, ObjectListing, GetObjectRequest}
import scala.collection.JavaConversions.{collectionAsScalaIterable => asScala}
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

object Ingest {
  
  lazy val s3 = new AmazonS3Client(
      new BasicAWSCredentials(
          System.getenv("AWS_ACCESS_KEY_ID"),
          System.getenv("AWS_SECRET_ACCESS_KEY")))
  
  lazy val brainWaves = 
      new BufferedSource(new Socket(
          InetAddress.getByName("localhost"), MINDFLEX_PORT)
          .getInputStream).getLines.toStream.filter(!_.isEmpty)
          .map(_.split(",").toList.map(_.toDouble)) filter (_(0) == 0) map (_.tail)
          // TODO map MindflexAlpha.brainFrame
          
  def s3List[T](s3: AmazonS3Client, bucket: String, prefix: String)(f: (S3ObjectSummary) => T) = {

    def scan(acc:List[T], listing:ObjectListing): List[T] = {
      val summaries = asScala[S3ObjectSummary](listing.getObjectSummaries())
      val mapped = (for (summary <- summaries) yield f(summary)).toList

      if (!listing.isTruncated) mapped.toList
      else scan(acc ::: mapped, s3.listNextBatchOfObjects(listing))
    }

    scan(List(), s3.listObjects(bucket, prefix))
  }
  
  lazy val unarchivedBrainwaves =
    s3List(s3,S3_BUCKET,S3_PREFIX)(_.getKey) filter {_.endsWith(".gz")} flatMap { key =>
      Source.fromInputStream(
          new GZIPInputStream(
              s3.getObject(S3_BUCKET,key).getObjectContent)).getLines.toList.
              filter {_.matches(".*com\\.xbarr\\.mindflex\\.MindflexAlpha.*:(\\s[\\d]+\\.[\\d]+){10}")}
    } map {_.split(".*com\\.xbarr\\.mindflex\\.MindflexAlpha.*:")(1).split("\\s+"). 
     filter{_.size > 0} map {_.toDouble} toList }
}