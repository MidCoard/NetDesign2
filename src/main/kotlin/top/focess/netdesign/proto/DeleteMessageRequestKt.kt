// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializedeleteMessageRequest")
public inline fun deleteMessageRequest(block: top.focess.netdesign.proto.DeleteMessageRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest =
  top.focess.netdesign.proto.DeleteMessageRequestKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 25 client packet
 * ```
 *
 * Protobuf type `netdesign2.DeleteMessageRequest`
 */
public object DeleteMessageRequestKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest = _builder.build()

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

    /**
     * ```
     * message id
     * ```
     *
     * `int32 id = 2;`
     */
    public var id: kotlin.Int
      @JvmName("getId")
      get() = _builder.getId()
      @JvmName("setId")
      set(value) {
        _builder.setId(value)
      }
    /**
     * ```
     * message id
     * ```
     *
     * `int32 id = 2;`
     */
    public fun clearId() {
      _builder.clearId()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest.copy(block: `top.focess.netdesign.proto`.DeleteMessageRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest =
  `top.focess.netdesign.proto`.DeleteMessageRequestKt.Dsl._create(this.toBuilder()).apply { block() }._build()

