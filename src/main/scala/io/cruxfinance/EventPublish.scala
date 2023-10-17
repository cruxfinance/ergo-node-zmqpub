import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.satergo.ergonnection.ErgoSocket
import com.satergo.ergonnection.records.Peer
import com.satergo.ergonnection.Version

import java.net.{SocketException, URI}
import com.satergo.ergonnection.messages.Inv
import com.satergo.ergonnection.modifiers.ErgoTransaction

import java.util.HexFormat
import java.util.stream.Collectors
import com.satergo.ergonnection.protocol.ProtocolMessage
import com.satergo.ergonnection.modifiers.Header
import com.satergo.ergonnection.messages.ModifierRequest
import io.cruxfinance.types.Config
import org.zeromq.ZContext
import org.zeromq.SocketType
import java.net.InetSocketAddress
import com.satergo.ergonnection.messages.ModifierResponse

object EventPublish {
  def main(args: Array[String]) = {

    val config = Config.read("config.json")

    val nodeURL = config.nodeURL
    val nodePort = config.nodePeersPort

    val nodeURI = new URI(nodeURL)

    val zeroMQIP = config.zmqIP
    val zeroMQPort = config.zmqPort

    var ergoSocket = new ErgoSocket(
      new InetSocketAddress(nodeURI.getHost, nodePort.toInt),
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
                  .toList
                println(
                  f"[${hhmmss()}] Received ID(s) of transaction(s) in Inv message: ${txIds
                      .stream()
                      .collect(Collectors.joining(", "))}"
                );
                txIds.forEach(txId => {
                  socket.sendMore("mempool")
                  socket.send(txId)
                })
                ergoSocket.send(
                  new ModifierRequest(ErgoTransaction.TYPE_ID, inv.elements())
                );
              case Header.TYPE_ID =>
                val headerIds = inv
                  .elements()
                  .stream()
                  .map(HexFormat.of().formatHex(_))
                  .toList
                println(
                  f"[${hhmmss()}] Received ID(s) of headers(s) in Inv message: ${headerIds
                      .stream()
                      .collect(Collectors.joining(", "))}"
                );
                headerIds.forEach(headerId => {
                  socket.sendMore("newBlock")
                  socket.send(headerId)
                })
                ergoSocket.send(
                  new ModifierRequest(Header.TYPE_ID, inv.elements())
                );
              case _ =>
            }
          }
          case mod: ModifierResponse => {
            mod
              .rawModifiers()
              .forEach(rm => {
                rm.typeId() match {
                  case Header.TYPE_ID => {
                    try {
                      val header = Header.deserialize(rm.id(), rm.`object`())
                      socket.sendMore("newHeight")
                      socket.sendMore(HexFormat.of.formatHex(header.id()))
                      socket.send(header.height().toString())
                    } catch {
                      case _ ->
                    }
                  }
                  case _ =>
                }
              })
          }
          case _: ProtocolMessage =>

        }
      } catch {
        case se: SocketException => {
          println("Socket failed, attempting to reconnect");
          try {
            ergoSocket.close();
          } catch {
            case e: Exception => println(f"Closing socket: ${e.getMessage}");
          }
          ergoSocket = new ErgoSocket(
            new InetSocketAddress(nodeURI.getHost, nodePort.toInt),
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
        case iae: IllegalArgumentException => {
          println("Received incorrect magic, attempting to reconnect");
          try {
            ergoSocket.close();
          } catch {
            case e: Exception => println(f"Closing socket: ${e.getMessage}");
          }
          ergoSocket = new ErgoSocket(
            new InetSocketAddress(nodeURI.getHost, nodePort.toInt),
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
        case e: Exception => throw e;
      }
    }
  }

  def hhmmss() = {
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
  }
}
