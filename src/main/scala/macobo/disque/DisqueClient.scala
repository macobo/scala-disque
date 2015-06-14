package macobo.disque

import com.redis.Redis
import com.redis.serialization.Parse.{Implicits => Parsers}
import macobo.disque.commands.AddRemoveJobs

trait Disque extends Redis with DisqueProtocol

trait DisqueCommand
extends Disque
with AddRemoveJobs
{
  override def initialize: Boolean = connect
}

class DisqueClient(
  override val host: String = "localhost",
  override val port: Int = 7711,
  override val timeout: Int = 0
)
extends DisqueCommand
