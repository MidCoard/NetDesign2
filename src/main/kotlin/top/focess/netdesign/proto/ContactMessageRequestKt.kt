// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializecontactMessageRequest")
public inline fun contactMessageRequest(block: top.focess.netdesign.proto.ContactMessageRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest =
  top.focess.netdesign.proto.ContactMessageRequestKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 12
 * ```
 *
 * Protobuf type `netdesign2.ContactMessageRequest`
 */
public object ContactMessageRequestKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest = _builder.build()

    /**
     * `int32 id = 1;`
     */
    public var id: kotlin.Int
      @JvmName("getId")
      get() = _builder.getId()
      @JvmName("setId")
      set(value) {
        _builder.setId(value)
      }
    /**
     * `int32 id = 1;`
     */
    public fun clearId() {
      _builder.clearId()
    }

    /**
     * `int32 internalId = 2;`
     */
    public var internalId: kotlin.Int
      @JvmName("getInternalId")
      get() = _builder.getInternalId()
      @JvmName("setInternalId")
      set(value) {
        _builder.setInternalId(value)
      }
    /**
     * `int32 internalId = 2;`
     */
    public fun clearInternalId() {
      _builder.clearInternalId()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest.copy(block: `top.focess.netdesign.proto`.ContactMessageRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ContactMessageRequest =
  `top.focess.netdesign.proto`.ContactMessageRequestKt.Dsl._create(this.toBuilder()).apply { block() }._build()

