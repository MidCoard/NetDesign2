// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: src/main/resources/protos/packet.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package top.focess.netdesign.proto;

@kotlin.jvm.JvmName("-initializefileDownloadResponse")
public inline fun fileDownloadResponse(block: top.focess.netdesign.proto.FileDownloadResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse =
  top.focess.netdesign.proto.FileDownloadResponseKt.Dsl._create(top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse.newBuilder()).apply { block() }._build()
/**
 * ```
 *packet id = 21 server packet
 * ```
 *
 * Protobuf type `netdesign2.FileDownloadResponse`
 */
public object FileDownloadResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse = _builder.build()

    /**
     * `.netdesign2.File file = 1;`
     */
    public var file: top.focess.netdesign.proto.PacketOuterClass.File
      @JvmName("getFile")
      get() = _builder.getFile()
      @JvmName("setFile")
      set(value) {
        _builder.setFile(value)
      }
    /**
     * `.netdesign2.File file = 1;`
     */
    public fun clearFile() {
      _builder.clearFile()
    }
    /**
     * `.netdesign2.File file = 1;`
     * @return Whether the file field is set.
     */
    public fun hasFile(): kotlin.Boolean {
      return _builder.hasFile()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse.copy(block: `top.focess.netdesign.proto`.FileDownloadResponseKt.Dsl.() -> kotlin.Unit): top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse =
  `top.focess.netdesign.proto`.FileDownloadResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponseOrBuilder.fileOrNull: top.focess.netdesign.proto.PacketOuterClass.File?
  get() = if (hasFile()) getFile() else null
