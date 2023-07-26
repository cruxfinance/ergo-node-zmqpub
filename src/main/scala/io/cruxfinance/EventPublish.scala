import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.satergo.ergonnection.ErgoSocket
import com.satergo.ergonnection.records.Peer
import com.satergo.ergonnection.Version
import java.net.InetSocketAddress
import com.satergo.ergonnection.messages.Inv
import com.satergo.ergonnection.modifiers.ErgoTransaction
import java.util.HexFormat
import java.util.stream.Collectors
import com.satergo.ergonnection.protocol.ProtocolMessage
import com.satergo.ergonnection.modifiers.Header
import java.net.SocketException
import com.satergo.ergonnection.messages.ModifierRequest
import org.zeromq.ZContext
import org.zeromq.SocketType

object EventPublish {
  def main(args: Array[String]) = {

    val nodeIP = sys.env.getOrElse("ERGO_NODE_IP", "127.0.0.1")
    val nodePort = Integer.parseInt(sys.env.getOrElse("ERGO_NODE_PORT", "9030"))

    val zeroMQIP = sys.env.getOrElse("ZMQ_IP", "0.0.0.0")
    val zeroMQPort = sys.env.getOrElse("ZMQ_PORT", "9060")

    var ergoSocket = new ErgoSocket(
      new InetSocketAddress(nodeIP, nodePort),
      new Peer(
        "ergoref",
        "ergo-mainnet-5.0.12",
        Version.parse("5.0.12"),
        ErgoSocket.BASIC_FEATURE_SET
      )
    )

    ergoSocket.sendHandshake()
    ergoSocket.acceptHandshake()

    val zContext: ZContext = new ZContext()
    val socket = zContext.createSocket(SocketType.PUB)
    socket.bind(f"tcp://${zeroMQIP}:${zeroMQPort}")

    println(f"Peer info: ${ergoSocket.getPeerInfo()}");

    while (true) {
      try {
        val msg = ergoSocket.acceptMessage();
        msg match {
          case inv: Inv => {
            inv.typeId() match {
              case ErgoTransaction.TYPE_ID =>
                val txIds = inv
                  .elements()
                  .stream()
                  .map(HexFormat.of().formatHex(_))
                  .toList()
                println(
                  f"[${hhmmss()}] Received ID(s) of transaction(s) in Inv message: ${txIds
                      .stream()
                      .collect(Collectors.joining(", "))}"
                );
                txIds.forEach(txId => socket.send(s"utx${txId}"))
                ergoSocket.send(
                  new ModifierRequest(ErgoTransaction.TYPE_ID, inv.elements())
                );
              case Header.TYPE_ID =>
                val headerIds = inv
                  .elements()
                  .stream()
                  .map(HexFormat.of().formatHex(_))
                  .toList()
                println(
                  f"[${hhmmss()}] Received ID(s) of headers(s) in Inv message: ${headerIds
                      .stream()
                      .collect(Collectors.joining(", "))}"
                );
                headerIds.forEach(headerId => socket.send(s"hdr${headerId}"))
              case _ =>
            }
          }
          case _: ProtocolMessage =>

        }
      } catch {
        case se: SocketException => {
          println("Attempting to reconnect");
          try {
            ergoSocket.close();
          } catch {
            case e: Exception => println(f"Closing socket: ${e.getMessage()}");
          }
          ergoSocket = new ErgoSocket(
            new InetSocketAddress("192.168.1.137", 9030),
            new Peer(
              "ergoref",
              "ergo-mainnet-5.0.12",
              Version.parse("5.0.12"),
              ErgoSocket.BASIC_FEATURE_SET
            )
          );
          ergoSocket.sendHandshake();
          ergoSocket.acceptHandshake();
        }
        case iae: IllegalArgumentException =>
          println(f"Error occured: ${iae.getMessage()}")
        case e: Exception => throw e;
      }
    }
  }

  def hhmmss() = {
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
  }
}
