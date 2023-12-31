// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: invoices.proto

package org.example.protos;

public final class InvoicesProtos {
  private InvoicesProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Invoice_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Invoice_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_InvoiceItem_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_InvoiceItem_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Invoices_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Invoices_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016invoices.proto\"\230\002\n\007Invoice\022\027\n\ninvoice_" +
      "no\030\001 \001(\tH\000\210\001\001\022\025\n\010datetime\030\002 \001(\tH\001\210\001\001\022\030\n\013" +
      "customer_id\030\003 \001(\005H\002\210\001\001\022\022\n\005email\030\004 \001(\tH\003\210" +
      "\001\001\022\030\n\013total_value\030\005 \001(\005H\004\210\001\001\022\035\n\020discount" +
      "ed_value\030\006 \001(\005H\005\210\001\001\022\033\n\005items\030\007 \003(\0132\014.Inv" +
      "oiceItemB\r\n\013_invoice_noB\013\n\t_datetimeB\016\n\014" +
      "_customer_idB\010\n\006_emailB\016\n\014_total_valueB\023" +
      "\n\021_discounted_value\"\321\001\n\013InvoiceItem\022\024\n\007i" +
      "tem_id\030\001 \001(\005H\000\210\001\001\022\025\n\010category\030\002 \001(\tH\001\210\001\001" +
      "\022\022\n\005brand\030\003 \001(\tH\002\210\001\001\022\022\n\005model\030\004 \001(\tH\003\210\001\001" +
      "\022\022\n\005price\030\005 \001(\005H\004\210\001\001\022\025\n\010quantity\030\006 \001(\005H\005" +
      "\210\001\001B\n\n\010_item_idB\013\n\t_categoryB\010\n\006_brandB\010" +
      "\n\006_modelB\010\n\006_priceB\013\n\t_quantity\"&\n\010Invoi" +
      "ces\022\032\n\010invoices\030\001 \003(\0132\010.InvoiceB&\n\022org.e" +
      "xample.protosB\016InvoicesProtosP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_Invoice_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Invoice_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Invoice_descriptor,
        new java.lang.String[] { "InvoiceNo", "Datetime", "CustomerId", "Email", "TotalValue", "DiscountedValue", "Items", "InvoiceNo", "Datetime", "CustomerId", "Email", "TotalValue", "DiscountedValue", });
    internal_static_InvoiceItem_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_InvoiceItem_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_InvoiceItem_descriptor,
        new java.lang.String[] { "ItemId", "Category", "Brand", "Model", "Price", "Quantity", "ItemId", "Category", "Brand", "Model", "Price", "Quantity", });
    internal_static_Invoices_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_Invoices_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Invoices_descriptor,
        new java.lang.String[] { "Invoices", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
