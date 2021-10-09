package org.multiground.mglinker

import com.velocitypowered.api.proxy.ProxyServer
import de.leonhard.storage.Json
import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetServer


class TcpServer(serve: ProxyServer, file: Json): AbstractVerticle() {
    private val server: ProxyServer = serve
    private val config = file
    @Throws(Exception::class)
    override fun start() {
        val server: NetServer = vertx.createNetServer()
        server.connectHandler {
            println("Got Connection!")

            it.handler { buf ->
                val input = buf.getString(0, buf.length())
                println("Got data $input from socket.")

                val parsed = input.split("-")

                val outBuffer = Buffer.buffer()
                when(parsed[0]){
                    "balance" -> {
                        val balance = this.config.getDouble("${parsed[1]}.${parsed[2]}.balance")
                        outBuffer.appendString("result-${parsed[1]}-${parsed[2]}-${balance}")
                    }
                    "deposit" -> {
                        //deposit-giftshower-aurum-10.0
                        val balance = this.config.getDouble("${parsed[1]}.${parsed[2]}.balance")
                        outBuffer.appendString("result-${parsed[1]}-${parsed[2]}-${balance + parsed[3].toDouble()}-${parsed[3]}")
                        this.config.set("${parsed[1]}.${parsed[2]}.balance", balance + parsed[3].toDouble())
                    }
                    "withdraw" -> {
                        val balance = this.config.getDouble("${parsed[1]}.${parsed[2]}.balance")
                        if(balance > parsed[3].toDouble() && parsed[3].toDouble() > 0) {
                            outBuffer.appendString("result-${parsed[1]}-${parsed[2]}-${balance - parsed[3].toDouble()}-${parsed[3]}")
                            this.config.set("${parsed[1]}.${parsed[2]}.balance", balance - parsed[3].toDouble())
                        }  else outBuffer.appendString("result-failure")
                    }
                    "has" -> {
                        val str = if(this.config.contains(parsed[1])) "true" else "false"
                        outBuffer.appendString("result-$str")
                    }
                    "transact" -> {
                        //transact-target-destination-type-amount
                        val balance = this.config.getDouble("${parsed[1]}.${parsed[3]}.balance")
                        val destBal = this.config.getDouble("${parsed[2]}.${parsed[3]}.balance")
                        if(balance > parsed[4].toDouble()){
                            outBuffer.appendString("result-${parsed[1]}-${parsed[3]}-${balance - parsed[4].toDouble()}-${parsed[3]}")
                            this.config.set("${parsed[1]}.${parsed[3]}.balance", balance - parsed[4].toDouble())
                            this.config.set("${parsed[2]}.${parsed[3]}.balance", destBal + parsed[4].toDouble())
                        } else outBuffer.appendString("result-failure")
                    }
                }
                it.write(outBuffer)
            }
        }
        server.listen(30001)
    }
}