// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializesetupChannelRequest")
public inline fun setupChannelRequest(block: top.focess.netdesign.proto.SetupChannelRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest =
  top.focess.netdesign.proto.SetupChannelRequestKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 16 client packet0 return ServerAckResponse
 * ```
 *
 * Protobuf type `netdesign2.SetupChannelRequest`
 */
public object SetupChannelRequestKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest = _builder.build()

    /**
     * `string token = 1;`
     */
    public var token: kotlin.String
      @JvmName("getToken")
      get() = _builder.getToken()
      @JvmName("setToken")
      set(value) {
        _builder.setToken(value)
      }
    /**
     * `string token = 1;`
     */
    public fun clearToken() {
      _builder.clearToken()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest.copy(block: `top.focess.netdesign.proto`.SetupChannelRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.SetupChannelRequest =
  `top.focess.netdesign.proto`.SetupChannelRequestKt.Dsl._create(this.toBuilder()).apply { block() }._build()

