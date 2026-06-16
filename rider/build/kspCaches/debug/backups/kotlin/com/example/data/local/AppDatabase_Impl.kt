package com.example.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _platformDao: Lazy<PlatformDao> = lazy {
    PlatformDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6,
        "5ca8344110161fb3f8c0b355be4093af", "81967042f0ee6784b49046685d330a66") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `restaurants` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `cuisine` TEXT NOT NULL, `rating` REAL NOT NULL, `deliveryTime` INTEGER NOT NULL, `deliveryFee` REAL NOT NULL, `image` TEXT NOT NULL, `bannerImage` TEXT NOT NULL, `address` TEXT NOT NULL, `isVeg` INTEGER NOT NULL, `isPromoted` INTEGER NOT NULL, `distanceKm` REAL NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_restaurants_cuisine` ON `restaurants` (`cuisine`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_restaurants_rating` ON `restaurants` (`rating`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_restaurants_isVeg` ON `restaurants` (`isVeg`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_restaurants_isPromoted` ON `restaurants` (`isPromoted`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dishes` (`id` INTEGER NOT NULL, `restaurantId` INTEGER NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `description` TEXT NOT NULL, `image` TEXT NOT NULL, `category` TEXT NOT NULL, `isVeg` INTEGER NOT NULL, `isBestseller` INTEGER NOT NULL, `spiceLevelSupport` INTEGER NOT NULL, `addonsJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dishes_restaurantId` ON `dishes` (`restaurantId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dishes_category` ON `dishes` (`category`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dishes_isBestseller` ON `dishes` (`isBestseller`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cart_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishId` INTEGER NOT NULL, `restaurantId` INTEGER NOT NULL, `restaurantName` TEXT NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `quantity` INTEGER NOT NULL, `spiceLevel` TEXT NOT NULL, `addons` TEXT NOT NULL, `notes` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `restaurantId` INTEGER NOT NULL, `restaurantName` TEXT NOT NULL, `status` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `itemsSummary` TEXT NOT NULL, `itemsDetailJson` TEXT NOT NULL, `paymentMethod` TEXT NOT NULL, `deliveryAddress` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `driverName` TEXT NOT NULL, `driverPhone` TEXT NOT NULL, `driverLat` REAL NOT NULL, `driverLng` REAL NOT NULL, `customerLat` REAL NOT NULL, `customerLng` REAL NOT NULL, `restaurantLat` REAL NOT NULL, `restaurantLng` REAL NOT NULL, `ratingGiven` REAL NOT NULL, `reviewText` TEXT NOT NULL, `reviewSentiment` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_timestamp` ON `orders` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_status` ON `orders` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_restaurantId` ON `orders` (`restaurantId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `wallet_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `memberName` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_wallet_transactions_timestamp` ON `wallet_transactions` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_wallet_transactions_type` ON `wallet_transactions` (`type`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `family_members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `avatarColor` INTEGER NOT NULL, `spendingLimit` REAL NOT NULL, `monthlySpent` REAL NOT NULL, `isAdmin` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `joinedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `family_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `memberId` INTEGER NOT NULL, `memberName` TEXT NOT NULL, `orderId` INTEGER NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `payment_methods` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `label` TEXT NOT NULL, `maskedValue` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `phone` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `avatarUrl` TEXT NOT NULL, `sessionToken` TEXT NOT NULL, `isVerified` INTEGER NOT NULL, `defaultAddressId` INTEGER NOT NULL, `isGoldMember` INTEGER NOT NULL, `walletBalance` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `lastLoginAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_phone` ON `users` (`phone`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `saved_addresses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `label` TEXT NOT NULL, `fullAddress` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `isDefault` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_addresses_userId` ON `saved_addresses` (`userId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_addresses_isDefault` ON `saved_addresses` (`isDefault`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `imageResName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '5ca8344110161fb3f8c0b355be4093af')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `restaurants`")
        connection.execSQL("DROP TABLE IF EXISTS `dishes`")
        connection.execSQL("DROP TABLE IF EXISTS `cart_items`")
        connection.execSQL("DROP TABLE IF EXISTS `orders`")
        connection.execSQL("DROP TABLE IF EXISTS `wallet_transactions`")
        connection.execSQL("DROP TABLE IF EXISTS `chat_messages`")
        connection.execSQL("DROP TABLE IF EXISTS `family_members`")
        connection.execSQL("DROP TABLE IF EXISTS `family_transactions`")
        connection.execSQL("DROP TABLE IF EXISTS `payment_methods`")
        connection.execSQL("DROP TABLE IF EXISTS `users`")
        connection.execSQL("DROP TABLE IF EXISTS `saved_addresses`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsRestaurants: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRestaurants.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("cuisine", TableInfo.Column("cuisine", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("rating", TableInfo.Column("rating", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("deliveryTime", TableInfo.Column("deliveryTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("deliveryFee", TableInfo.Column("deliveryFee", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("image", TableInfo.Column("image", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("bannerImage", TableInfo.Column("bannerImage", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("address", TableInfo.Column("address", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("isVeg", TableInfo.Column("isVeg", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("isPromoted", TableInfo.Column("isPromoted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("distanceKm", TableInfo.Column("distanceKm", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRestaurants.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRestaurants: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRestaurants: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRestaurants.add(TableInfo.Index("index_restaurants_cuisine", false,
            listOf("cuisine"), listOf("ASC")))
        _indicesRestaurants.add(TableInfo.Index("index_restaurants_rating", false, listOf("rating"),
            listOf("ASC")))
        _indicesRestaurants.add(TableInfo.Index("index_restaurants_isVeg", false, listOf("isVeg"),
            listOf("ASC")))
        _indicesRestaurants.add(TableInfo.Index("index_restaurants_isPromoted", false,
            listOf("isPromoted"), listOf("ASC")))
        val _infoRestaurants: TableInfo = TableInfo("restaurants", _columnsRestaurants,
            _foreignKeysRestaurants, _indicesRestaurants)
        val _existingRestaurants: TableInfo = read(connection, "restaurants")
        if (!_infoRestaurants.equals(_existingRestaurants)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |restaurants(com.example.data.local.RestaurantEntity).
              | Expected:
              |""".trimMargin() + _infoRestaurants + """
              |
              | Found:
              |""".trimMargin() + _existingRestaurants)
        }
        val _columnsDishes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDishes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("restaurantId", TableInfo.Column("restaurantId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("price", TableInfo.Column("price", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("image", TableInfo.Column("image", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("isVeg", TableInfo.Column("isVeg", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("isBestseller", TableInfo.Column("isBestseller", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("spiceLevelSupport", TableInfo.Column("spiceLevelSupport", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDishes.put("addonsJson", TableInfo.Column("addonsJson", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDishes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDishes: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDishes.add(TableInfo.Index("index_dishes_restaurantId", false,
            listOf("restaurantId"), listOf("ASC")))
        _indicesDishes.add(TableInfo.Index("index_dishes_category", false, listOf("category"),
            listOf("ASC")))
        _indicesDishes.add(TableInfo.Index("index_dishes_isBestseller", false,
            listOf("isBestseller"), listOf("ASC")))
        val _infoDishes: TableInfo = TableInfo("dishes", _columnsDishes, _foreignKeysDishes,
            _indicesDishes)
        val _existingDishes: TableInfo = read(connection, "dishes")
        if (!_infoDishes.equals(_existingDishes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dishes(com.example.data.local.DishEntity).
              | Expected:
              |""".trimMargin() + _infoDishes + """
              |
              | Found:
              |""".trimMargin() + _existingDishes)
        }
        val _columnsCartItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCartItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("dishId", TableInfo.Column("dishId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("restaurantId", TableInfo.Column("restaurantId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("restaurantName", TableInfo.Column("restaurantName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("price", TableInfo.Column("price", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("quantity", TableInfo.Column("quantity", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("spiceLevel", TableInfo.Column("spiceLevel", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("addons", TableInfo.Column("addons", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCartItems.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCartItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCartItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCartItems: TableInfo = TableInfo("cart_items", _columnsCartItems,
            _foreignKeysCartItems, _indicesCartItems)
        val _existingCartItems: TableInfo = read(connection, "cart_items")
        if (!_infoCartItems.equals(_existingCartItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cart_items(com.example.data.local.CartItemEntity).
              | Expected:
              |""".trimMargin() + _infoCartItems + """
              |
              | Found:
              |""".trimMargin() + _existingCartItems)
        }
        val _columnsOrders: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOrders.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("restaurantId", TableInfo.Column("restaurantId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("restaurantName", TableInfo.Column("restaurantName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("totalAmount", TableInfo.Column("totalAmount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("itemsSummary", TableInfo.Column("itemsSummary", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("itemsDetailJson", TableInfo.Column("itemsDetailJson", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("paymentMethod", TableInfo.Column("paymentMethod", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("deliveryAddress", TableInfo.Column("deliveryAddress", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("driverName", TableInfo.Column("driverName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("driverPhone", TableInfo.Column("driverPhone", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("driverLat", TableInfo.Column("driverLat", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("driverLng", TableInfo.Column("driverLng", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("customerLat", TableInfo.Column("customerLat", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("customerLng", TableInfo.Column("customerLng", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("restaurantLat", TableInfo.Column("restaurantLat", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("restaurantLng", TableInfo.Column("restaurantLng", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("ratingGiven", TableInfo.Column("ratingGiven", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("reviewText", TableInfo.Column("reviewText", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOrders.put("reviewSentiment", TableInfo.Column("reviewSentiment", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOrders: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesOrders: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesOrders.add(TableInfo.Index("index_orders_timestamp", false, listOf("timestamp"),
            listOf("ASC")))
        _indicesOrders.add(TableInfo.Index("index_orders_status", false, listOf("status"),
            listOf("ASC")))
        _indicesOrders.add(TableInfo.Index("index_orders_restaurantId", false,
            listOf("restaurantId"), listOf("ASC")))
        val _infoOrders: TableInfo = TableInfo("orders", _columnsOrders, _foreignKeysOrders,
            _indicesOrders)
        val _existingOrders: TableInfo = read(connection, "orders")
        if (!_infoOrders.equals(_existingOrders)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |orders(com.example.data.local.OrderEntity).
              | Expected:
              |""".trimMargin() + _infoOrders + """
              |
              | Found:
              |""".trimMargin() + _existingOrders)
        }
        val _columnsWalletTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWalletTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWalletTransactions.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWalletTransactions.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWalletTransactions.put("description", TableInfo.Column("description", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWalletTransactions.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWalletTransactions.put("memberName", TableInfo.Column("memberName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWalletTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWalletTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWalletTransactions.add(TableInfo.Index("index_wallet_transactions_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        _indicesWalletTransactions.add(TableInfo.Index("index_wallet_transactions_type", false,
            listOf("type"), listOf("ASC")))
        val _infoWalletTransactions: TableInfo = TableInfo("wallet_transactions",
            _columnsWalletTransactions, _foreignKeysWalletTransactions, _indicesWalletTransactions)
        val _existingWalletTransactions: TableInfo = read(connection, "wallet_transactions")
        if (!_infoWalletTransactions.equals(_existingWalletTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |wallet_transactions(com.example.data.local.WalletTransactionEntity).
              | Expected:
              |""".trimMargin() + _infoWalletTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingWalletTransactions)
        }
        val _columnsChatMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChatMessages.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("sender", TableInfo.Column("sender", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("message", TableInfo.Column("message", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChatMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChatMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChatMessages: TableInfo = TableInfo("chat_messages", _columnsChatMessages,
            _foreignKeysChatMessages, _indicesChatMessages)
        val _existingChatMessages: TableInfo = read(connection, "chat_messages")
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chat_messages(com.example.data.local.ChatMessageEntity).
              | Expected:
              |""".trimMargin() + _infoChatMessages + """
              |
              | Found:
              |""".trimMargin() + _existingChatMessages)
        }
        val _columnsFamilyMembers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFamilyMembers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("email", TableInfo.Column("email", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("avatarColor", TableInfo.Column("avatarColor", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("spendingLimit", TableInfo.Column("spendingLimit", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("monthlySpent", TableInfo.Column("monthlySpent", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("isAdmin", TableInfo.Column("isAdmin", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyMembers.put("joinedAt", TableInfo.Column("joinedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFamilyMembers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFamilyMembers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFamilyMembers: TableInfo = TableInfo("family_members", _columnsFamilyMembers,
            _foreignKeysFamilyMembers, _indicesFamilyMembers)
        val _existingFamilyMembers: TableInfo = read(connection, "family_members")
        if (!_infoFamilyMembers.equals(_existingFamilyMembers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |family_members(com.example.data.local.FamilyMemberEntity).
              | Expected:
              |""".trimMargin() + _infoFamilyMembers + """
              |
              | Found:
              |""".trimMargin() + _existingFamilyMembers)
        }
        val _columnsFamilyTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFamilyTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("memberId", TableInfo.Column("memberId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("memberName", TableInfo.Column("memberName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("orderId", TableInfo.Column("orderId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("description", TableInfo.Column("description", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFamilyTransactions.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFamilyTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFamilyTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFamilyTransactions: TableInfo = TableInfo("family_transactions",
            _columnsFamilyTransactions, _foreignKeysFamilyTransactions, _indicesFamilyTransactions)
        val _existingFamilyTransactions: TableInfo = read(connection, "family_transactions")
        if (!_infoFamilyTransactions.equals(_existingFamilyTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |family_transactions(com.example.data.local.FamilyTransactionEntity).
              | Expected:
              |""".trimMargin() + _infoFamilyTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingFamilyTransactions)
        }
        val _columnsPaymentMethods: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPaymentMethods.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentMethods.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentMethods.put("label", TableInfo.Column("label", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentMethods.put("maskedValue", TableInfo.Column("maskedValue", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentMethods.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentMethods.put("addedAt", TableInfo.Column("addedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPaymentMethods: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPaymentMethods: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPaymentMethods: TableInfo = TableInfo("payment_methods", _columnsPaymentMethods,
            _foreignKeysPaymentMethods, _indicesPaymentMethods)
        val _existingPaymentMethods: TableInfo = read(connection, "payment_methods")
        if (!_infoPaymentMethods.equals(_existingPaymentMethods)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |payment_methods(com.example.data.local.PaymentMethodEntity).
              | Expected:
              |""".trimMargin() + _infoPaymentMethods + """
              |
              | Found:
              |""".trimMargin() + _existingPaymentMethods)
        }
        val _columnsUsers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("phone", TableInfo.Column("phone", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("email", TableInfo.Column("email", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("avatarUrl", TableInfo.Column("avatarUrl", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("sessionToken", TableInfo.Column("sessionToken", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("isVerified", TableInfo.Column("isVerified", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("defaultAddressId", TableInfo.Column("defaultAddressId", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("isGoldMember", TableInfo.Column("isGoldMember", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("walletBalance", TableInfo.Column("walletBalance", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("lastLoginAt", TableInfo.Column("lastLoginAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUsers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesUsers.add(TableInfo.Index("index_users_phone", true, listOf("phone"),
            listOf("ASC")))
        val _infoUsers: TableInfo = TableInfo("users", _columnsUsers, _foreignKeysUsers,
            _indicesUsers)
        val _existingUsers: TableInfo = read(connection, "users")
        if (!_infoUsers.equals(_existingUsers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |users(com.example.data.local.UserEntity).
              | Expected:
              |""".trimMargin() + _infoUsers + """
              |
              | Found:
              |""".trimMargin() + _existingUsers)
        }
        val _columnsSavedAddresses: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavedAddresses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("userId", TableInfo.Column("userId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("label", TableInfo.Column("label", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("fullAddress", TableInfo.Column("fullAddress", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedAddresses.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavedAddresses: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavedAddresses: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSavedAddresses.add(TableInfo.Index("index_saved_addresses_userId", false,
            listOf("userId"), listOf("ASC")))
        _indicesSavedAddresses.add(TableInfo.Index("index_saved_addresses_isDefault", false,
            listOf("isDefault"), listOf("ASC")))
        val _infoSavedAddresses: TableInfo = TableInfo("saved_addresses", _columnsSavedAddresses,
            _foreignKeysSavedAddresses, _indicesSavedAddresses)
        val _existingSavedAddresses: TableInfo = read(connection, "saved_addresses")
        if (!_infoSavedAddresses.equals(_existingSavedAddresses)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |saved_addresses(com.example.data.local.SavedAddressEntity).
              | Expected:
              |""".trimMargin() + _infoSavedAddresses + """
              |
              | Found:
              |""".trimMargin() + _existingSavedAddresses)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("imageResName", TableInfo.Column("imageResName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("colorHex", TableInfo.Column("colorHex", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories,
            _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.example.data.local.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "restaurants", "dishes",
        "cart_items", "orders", "wallet_transactions", "chat_messages", "family_members",
        "family_transactions", "payment_methods", "users", "saved_addresses", "categories")
  }

  public override fun clearAllTables() {
    super.performClear(false, "restaurants", "dishes", "cart_items", "orders",
        "wallet_transactions", "chat_messages", "family_members", "family_transactions",
        "payment_methods", "users", "saved_addresses", "categories")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(PlatformDao::class, PlatformDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun dao(): PlatformDao = _platformDao.value
}
