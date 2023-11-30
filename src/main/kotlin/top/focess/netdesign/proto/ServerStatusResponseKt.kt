// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializeserverStatusResponse")
public inline fun serverStatusResponse(block: top.focess.netdesign.proto.ServerStatusResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse =
  top.focess.netdesign.proto.ServerStatusResponseKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 1 server packet
 * ```
 *
 * Protobuf type `netdesign2.ServerStatusResponse`
 */
public object ServerStatusResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse = _builder.build()

    /**
     * `bool online = 1;`
     */
    public var online: kotlin.Boolean
      @JvmName("getOnline")
      get() = _builder.getOnline()
      @JvmName("setOnline")
      set(value) {
        _builder.setOnline(value)
      }
    /**
     * `bool online = 1;`
     */
    public fun clearOnline() {
      _builder.clearOnline()
    }

    /**
     * `bool registrable = 2;`
     */
    public var registrable: kotlin.Boolean
      @JvmName("getRegistrable")
      get() = _builder.getRegistrable()
      @JvmName("setRegistrable")
      set(value) {
        _builder.setRegistrable(value)
      }
    /**
     * `bool registrable = 2;`
     */
    public fun clearRegistrable() {
      _builder.clearRegistrable()
    }

    /**
     * `optional string serverPublicKey = 3;`
     */
    public var serverPublicKey: kotlin.String?
      @JvmName("getServerPublicKey")
      get() = _builder.getServerPublicKey()
      @JvmName("setServerPublicKey")
      set(value) {
        _builder.setServerPublicKey(value)
      }
    /**
     * `optional string serverPublicKey = 3;`
     */
    public fun clearServerPublicKey() {
      _builder.clearServerPublicKey()
    }
    /**
     * `optional string serverPublicKey = 3;`
     * @return Whether the serverPublicKey field is set.
     */
    public fun hasServerPublicKey(): kotlin.Boolean {
      return _builder.hasServerPublicKey()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse.copy(block: `top.focess.netdesign.proto`.ServerStatusResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse =
  `top.focess.netdesign.proto`.ServerStatusResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()

