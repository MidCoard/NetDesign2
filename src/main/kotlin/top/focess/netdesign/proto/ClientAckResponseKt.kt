// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializeclientAckResponse")
public inline fun clientAckResponse(block: top.focess.netdesign.proto.ClientAckResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse =
  top.focess.netdesign.proto.ClientAckResponseKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 9 client packet0
 * ```
 *
 * Protobuf type `netdesign2.ClientAckResponse`
 */
public object ClientAckResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse = _builder.build()
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse.copy(block: `top.focess.netdesign.proto`.ClientAckResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ClientAckResponse =
  `top.focess.netdesign.proto`.ClientAckResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()
