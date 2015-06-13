import com.disque._

val client = new DisqueClient()
//val resp = client.hello()
//resp.results.foreach { println(_) }
client.addJob("testQueue", "hahaJob", 50000)
