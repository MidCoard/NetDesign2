// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializeloginPreRequest")
public inline fun loginPreRequest(block: top.focess.netdesign.proto.LoginPreRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest =
  top.focess.netdesign.proto.LoginPreRequestKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 2 client packet
 * ```
 *
 * Protobuf type `netdesign2.LoginPreRequest`
 */
public object LoginPreRequestKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest = _builder.build()

    /**
     * `string username = 1;`
     */
    public var username: kotlin.String
      @JvmName("getUsername")
      get() = _builder.getUsername()
      @JvmName("setUsername")
      set(value) {
        _builder.setUsername(value)
      }
    /**
     * `string username = 1;`
     */
    public fun clearUsername() {
      _builder.clearUsername()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest.copy(block: `top.focess.netdesign.proto`.LoginPreRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.LoginPreRequest =
  `top.focess.netdesign.proto`.LoginPreRequestKt.Dsl._create(this.toBuilder()).apply { block() }._build()

