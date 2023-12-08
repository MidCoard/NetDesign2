// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializecontactMessagesRequest")
public inline fun contactMessagesRequest(block: top.focess.netdesign.proto.ContactMessagesRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest =
  top.focess.netdesign.proto.ContactMessagesRequestKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 18 server packet0 return ClientAckResponse
 * ```
 *
 * Protobuf type `netdesign2.ContactMessagesRequest`
 */
public object ContactMessagesRequestKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest = _builder.build()

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class MessagesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated .netdesign2.Message messages = 1;`
     */
     public val messages: com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getMessagesList()
      )
    /**
     * `repeated .netdesign2.Message messages = 1;`
     * @param value The messages to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addMessages")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.add(value: top.focess.netdesign.proto.PacketOuterClass.Message) {
      _builder.addMessages(value)
    }
    /**
     * `repeated .netdesign2.Message messages = 1;`
     * @param value The messages to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignMessages")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.plusAssign(value: top.focess.netdesign.proto.PacketOuterClass.Message) {
      add(value)
    }
    /**
     * `repeated .netdesign2.Message messages = 1;`
     * @param values The messages to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllMessages")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.addAll(values: kotlin.collections.Iterable<top.focess.netdesign.proto.PacketOuterClass.Message>) {
      _builder.addAllMessages(values)
    }
    /**
     * `repeated .netdesign2.Message messages = 1;`
     * @param values The messages to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllMessages")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.plusAssign(values: kotlin.collections.Iterable<top.focess.netdesign.proto.PacketOuterClass.Message>) {
      addAll(values)
    }
    /**
     * `repeated .netdesign2.Message messages = 1;`
     * @param index The index to set the value at.
     * @param value The messages to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setMessages")
    public operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.set(index: kotlin.Int, value: top.focess.netdesign.proto.PacketOuterClass.Message) {
      _builder.setMessages(index, value)
    }
    /**
     * `repeated .netdesign2.Message messages = 1;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearMessages")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Message, MessagesProxy>.clear() {
      _builder.clearMessages()
    }

  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest.copy(block: `top.focess.netdesign.proto`.ContactMessagesRequestKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.ContactMessagesRequest =
  `top.focess.netdesign.proto`.ContactMessagesRequestKt.Dsl._create(this.toBuilder()).apply { block() }._build()

