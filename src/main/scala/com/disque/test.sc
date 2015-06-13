import com.disque._

val client = new DisqueClient()
val resp = client.hello()
resp.results.foreach { println(_) }
