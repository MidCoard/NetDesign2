// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializeserverStatusUpdateResponse")
public inline fun serverStatusUpdateResponse(block: top.focess.netdesign.proto.ServerStatusUpdateResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse =
  top.focess.netdesign.proto.ServerStatusUpdateResponseKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 7
 * ```
 *
 * Protobuf type `netdesign2.ServerStatusUpdateResponse`
 */
public object ServerStatusUpdateResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse = _builder.build()

    /**
     * `int32 packetId = 1;`
     */
    public var packetId: kotlin.Int
      @JvmName("getPacketId")
      get() = _builder.getPacketId()
      @JvmName("setPacketId")
      set(value) {
        _builder.setPacketId(value)
      }
    /**
     * `int32 packetId = 1;`
     */
    public fun clearPacketId() {
      _builder.clearPacketId()
    }

    /**
     * `bool online = 2;`
     */
    public var online: kotlin.Boolean
      @JvmName("getOnline")
      get() = _builder.getOnline()
      @JvmName("setOnline")
      set(value) {
        _builder.setOnline(value)
      }
    /**
     * `bool online = 2;`
     */
    public fun clearOnline() {
      _builder.clearOnline()
    }

    /**
     * `bool registrable = 3;`
     */
    public var registrable: kotlin.Boolean
      @JvmName("getRegistrable")
      get() = _builder.getRegistrable()
      @JvmName("setRegistrable")
      set(value) {
        _builder.setRegistrable(value)
      }
    /**
     * `bool registrable = 3;`
     */
    public fun clearRegistrable() {
      _builder.clearRegistrable()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse.copy(block: `top.focess.netdesign.proto`.ServerStatusUpdateResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ServerStatusUpdateResponse =
  `top.focess.netdesign.proto`.ServerStatusUpdateResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()

