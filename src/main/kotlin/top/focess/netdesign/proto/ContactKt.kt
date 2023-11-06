// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializecontact")
public inline fun contact(block: top.focess.netdesign.proto.ContactKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.Contact =
  top.focess.netdesign.proto.ContactKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.Contact.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `netdesign2.Contact`
 */
public object ContactKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.Contact.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.Contact.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.Contact = _builder.build()

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
     * `string name = 2;`
     */
    public var name: kotlin.String
      @JvmName("getName")
      get() = _builder.getName()
      @JvmName("setName")
      set(value) {
        _builder.setName(value)
      }
    /**
     * `string name = 2;`
     */
    public fun clearName() {
      _builder.clearName()
    }

    /**
     * `bool online = 3;`
     */
    public var online: kotlin.Boolean
      @JvmName("getOnline")
      get() = _builder.getOnline()
      @JvmName("setOnline")
      set(value) {
        _builder.setOnline(value)
      }
    /**
     * `bool online = 3;`
     */
    public fun clearOnline() {
      _builder.clearOnline()
    }

    /**
     * `.netdesign2.Contact.ContactType type = 4;`
     */
    public var type: top.focess.netdesign.proto.PacketOuterClass.Contact.ContactType
      @JvmName("getType")
      get() = _builder.getType()
      @JvmName("setType")
      set(value) {
        _builder.setType(value)
      }
    public var typeValue: kotlin.Int
      @JvmName("getTypeValue")
      get() = _builder.getTypeValue()
      @JvmName("setTypeValue")
      set(value) {
        _builder.setTypeValue(value)
      }
    /**
     * `.netdesign2.Contact.ContactType type = 4;`
     */
    public fun clearType() {
      _builder.clearType()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class MembersProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     */
     public val members: com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getMembersList()
      )
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     * @param value The members to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addMembers")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.add(value: top.focess.netdesign.proto.PacketOuterClass.Contact) {
      _builder.addMembers(value)
    }
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     * @param value The members to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignMembers")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.plusAssign(value: top.focess.netdesign.proto.PacketOuterClass.Contact) {
      add(value)
    }
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     * @param values The members to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllMembers")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.addAll(values: kotlin.collections.Iterable<top.focess.netdesign.proto.PacketOuterClass.Contact>) {
      _builder.addAllMembers(values)
    }
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     * @param values The members to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllMembers")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.plusAssign(values: kotlin.collections.Iterable<top.focess.netdesign.proto.PacketOuterClass.Contact>) {
      addAll(values)
    }
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     * @param index The index to set the value at.
     * @param value The members to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setMembers")
    public operator fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.set(index: kotlin.Int, value: top.focess.netdesign.proto.PacketOuterClass.Contact) {
      _builder.setMembers(index, value)
    }
    /**
     * ```
     * optional, only if type is GROUP
     * ```
     *
     * `repeated .netdesign2.Contact members = 5;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearMembers")
    public fun com.google.protobuf.kotlin.DslList<top.focess.netdesign.proto.PacketOuterClass.Contact, MembersProxy>.clear() {
      _builder.clearMembers()
    }

  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.Contact.copy(block: `top.focess.netdesign.proto`.ContactKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.Contact =
  `top.focess.netdesign.proto`.ContactKt.Dsl._create(this.toBuilder()).apply { block() }._build()

