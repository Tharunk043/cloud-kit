package com.example.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlatformDao_Impl(
  __db: RoomDatabase,
) : PlatformDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCategoryEntity: EntityInsertAdapter<CategoryEntity>

  private val __insertAdapterOfRestaurantEntity: EntityInsertAdapter<RestaurantEntity>

  private val __insertAdapterOfDishEntity: EntityInsertAdapter<DishEntity>

  private val __insertAdapterOfCartItemEntity: EntityInsertAdapter<CartItemEntity>

  private val __insertAdapterOfOrderEntity: EntityInsertAdapter<OrderEntity>

  private val __insertAdapterOfWalletTransactionEntity: EntityInsertAdapter<WalletTransactionEntity>

  private val __insertAdapterOfChatMessageEntity: EntityInsertAdapter<ChatMessageEntity>

  private val __insertAdapterOfFamilyMemberEntity: EntityInsertAdapter<FamilyMemberEntity>

  private val __insertAdapterOfFamilyTransactionEntity: EntityInsertAdapter<FamilyTransactionEntity>

  private val __insertAdapterOfPaymentMethodEntity: EntityInsertAdapter<PaymentMethodEntity>

  private val __insertAdapterOfUserEntity: EntityInsertAdapter<UserEntity>

  private val __insertAdapterOfSavedAddressEntity: EntityInsertAdapter<SavedAddressEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCategoryEntity = object : EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `categories` (`id`,`name`,`imageResName`,`colorHex`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.imageResName)
        statement.bindText(4, entity.colorHex)
      }
    }
    this.__insertAdapterOfRestaurantEntity = object : EntityInsertAdapter<RestaurantEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `restaurants` (`id`,`name`,`description`,`cuisine`,`rating`,`deliveryTime`,`deliveryFee`,`image`,`bannerImage`,`address`,`isVeg`,`isPromoted`,`distanceKm`,`latitude`,`longitude`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RestaurantEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.cuisine)
        statement.bindDouble(5, entity.rating.toDouble())
        statement.bindLong(6, entity.deliveryTime.toLong())
        statement.bindDouble(7, entity.deliveryFee)
        statement.bindText(8, entity.image)
        statement.bindText(9, entity.bannerImage)
        statement.bindText(10, entity.address)
        val _tmp: Int = if (entity.isVeg) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        val _tmp_1: Int = if (entity.isPromoted) 1 else 0
        statement.bindLong(12, _tmp_1.toLong())
        statement.bindDouble(13, entity.distanceKm.toDouble())
        statement.bindDouble(14, entity.latitude)
        statement.bindDouble(15, entity.longitude)
      }
    }
    this.__insertAdapterOfDishEntity = object : EntityInsertAdapter<DishEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `dishes` (`id`,`restaurantId`,`name`,`price`,`description`,`image`,`category`,`isVeg`,`isBestseller`,`spiceLevelSupport`,`addonsJson`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DishEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.restaurantId.toLong())
        statement.bindText(3, entity.name)
        statement.bindDouble(4, entity.price)
        statement.bindText(5, entity.description)
        statement.bindText(6, entity.image)
        statement.bindText(7, entity.category)
        val _tmp: Int = if (entity.isVeg) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        val _tmp_1: Int = if (entity.isBestseller) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.spiceLevelSupport) 1 else 0
        statement.bindLong(10, _tmp_2.toLong())
        statement.bindText(11, entity.addonsJson)
      }
    }
    this.__insertAdapterOfCartItemEntity = object : EntityInsertAdapter<CartItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `cart_items` (`id`,`dishId`,`restaurantId`,`restaurantName`,`name`,`price`,`quantity`,`spiceLevel`,`addons`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CartItemEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.dishId.toLong())
        statement.bindLong(3, entity.restaurantId.toLong())
        statement.bindText(4, entity.restaurantName)
        statement.bindText(5, entity.name)
        statement.bindDouble(6, entity.price)
        statement.bindLong(7, entity.quantity.toLong())
        statement.bindText(8, entity.spiceLevel)
        statement.bindText(9, entity.addons)
        statement.bindText(10, entity.notes)
      }
    }
    this.__insertAdapterOfOrderEntity = object : EntityInsertAdapter<OrderEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `orders` (`id`,`restaurantId`,`restaurantName`,`status`,`totalAmount`,`itemsSummary`,`itemsDetailJson`,`paymentMethod`,`deliveryAddress`,`timestamp`,`driverName`,`driverPhone`,`driverLat`,`driverLng`,`customerLat`,`customerLng`,`restaurantLat`,`restaurantLng`,`ratingGiven`,`reviewText`,`reviewSentiment`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: OrderEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.restaurantId.toLong())
        statement.bindText(3, entity.restaurantName)
        statement.bindText(4, entity.status)
        statement.bindDouble(5, entity.totalAmount)
        statement.bindText(6, entity.itemsSummary)
        statement.bindText(7, entity.itemsDetailJson)
        statement.bindText(8, entity.paymentMethod)
        statement.bindText(9, entity.deliveryAddress)
        statement.bindLong(10, entity.timestamp)
        statement.bindText(11, entity.driverName)
        statement.bindText(12, entity.driverPhone)
        statement.bindDouble(13, entity.driverLat)
        statement.bindDouble(14, entity.driverLng)
        statement.bindDouble(15, entity.customerLat)
        statement.bindDouble(16, entity.customerLng)
        statement.bindDouble(17, entity.restaurantLat)
        statement.bindDouble(18, entity.restaurantLng)
        statement.bindDouble(19, entity.ratingGiven.toDouble())
        statement.bindText(20, entity.reviewText)
        statement.bindText(21, entity.reviewSentiment)
      }
    }
    this.__insertAdapterOfWalletTransactionEntity = object :
        EntityInsertAdapter<WalletTransactionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `wallet_transactions` (`id`,`type`,`amount`,`description`,`timestamp`,`memberName`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WalletTransactionEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.type)
        statement.bindDouble(3, entity.amount)
        statement.bindText(4, entity.description)
        statement.bindLong(5, entity.timestamp)
        statement.bindText(6, entity.memberName)
      }
    }
    this.__insertAdapterOfChatMessageEntity = object : EntityInsertAdapter<ChatMessageEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `chat_messages` (`id`,`sender`,`message`,`timestamp`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatMessageEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.sender)
        statement.bindText(3, entity.message)
        statement.bindLong(4, entity.timestamp)
      }
    }
    this.__insertAdapterOfFamilyMemberEntity = object : EntityInsertAdapter<FamilyMemberEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `family_members` (`id`,`name`,`email`,`avatarColor`,`spendingLimit`,`monthlySpent`,`isAdmin`,`isActive`,`joinedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FamilyMemberEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.email)
        statement.bindLong(4, entity.avatarColor)
        statement.bindDouble(5, entity.spendingLimit)
        statement.bindDouble(6, entity.monthlySpent)
        val _tmp: Int = if (entity.isAdmin) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        statement.bindLong(9, entity.joinedAt)
      }
    }
    this.__insertAdapterOfFamilyTransactionEntity = object :
        EntityInsertAdapter<FamilyTransactionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `family_transactions` (`id`,`memberId`,`memberName`,`orderId`,`amount`,`description`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FamilyTransactionEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.memberId.toLong())
        statement.bindText(3, entity.memberName)
        statement.bindLong(4, entity.orderId.toLong())
        statement.bindDouble(5, entity.amount)
        statement.bindText(6, entity.description)
        statement.bindLong(7, entity.timestamp)
      }
    }
    this.__insertAdapterOfPaymentMethodEntity = object : EntityInsertAdapter<PaymentMethodEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `payment_methods` (`id`,`type`,`label`,`maskedValue`,`isDefault`,`addedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PaymentMethodEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.type)
        statement.bindText(3, entity.label)
        statement.bindText(4, entity.maskedValue)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.addedAt)
      }
    }
    this.__insertAdapterOfUserEntity = object : EntityInsertAdapter<UserEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `users` (`id`,`phone`,`name`,`email`,`avatarUrl`,`sessionToken`,`isVerified`,`defaultAddressId`,`isGoldMember`,`walletBalance`,`createdAt`,`lastLoginAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.phone)
        statement.bindText(3, entity.name)
        statement.bindText(4, entity.email)
        statement.bindText(5, entity.avatarUrl)
        statement.bindText(6, entity.sessionToken)
        val _tmp: Int = if (entity.isVerified) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.defaultAddressId.toLong())
        val _tmp_1: Int = if (entity.isGoldMember) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindDouble(10, entity.walletBalance)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.lastLoginAt)
      }
    }
    this.__insertAdapterOfSavedAddressEntity = object : EntityInsertAdapter<SavedAddressEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `saved_addresses` (`id`,`userId`,`label`,`fullAddress`,`latitude`,`longitude`,`isDefault`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SavedAddressEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.userId.toLong())
        statement.bindText(3, entity.label)
        statement.bindText(4, entity.fullAddress)
        statement.bindDouble(5, entity.latitude)
        statement.bindDouble(6, entity.longitude)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.createdAt)
      }
    }
  }

  public override suspend fun insertCategories(categories: List<CategoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity.insert(_connection, categories)
  }

  public override suspend fun insertRestaurants(restaurants: List<RestaurantEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRestaurantEntity.insert(_connection, restaurants)
  }

  public override suspend fun insertDishes(dishes: List<DishEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfDishEntity.insert(_connection, dishes)
  }

  public override suspend fun insertCartItem(item: CartItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfCartItemEntity.insert(_connection, item)
  }

  public override suspend fun insertOrder(order: OrderEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfOrderEntity.insertAndReturnId(_connection, order)
    _result
  }

  public override suspend fun insertWalletTransaction(tx: WalletTransactionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWalletTransactionEntity.insert(_connection, tx)
  }

  public override suspend fun insertChatMessage(msg: ChatMessageEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfChatMessageEntity.insert(_connection, msg)
  }

  public override suspend fun insertFamilyMember(member: FamilyMemberEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfFamilyMemberEntity.insertAndReturnId(_connection, member)
    _result
  }

  public override suspend fun insertFamilyTransaction(tx: FamilyTransactionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFamilyTransactionEntity.insert(_connection, tx)
  }

  public override suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPaymentMethodEntity.insertAndReturnId(_connection, method)
    _result
  }

  public override suspend fun insertUser(user: UserEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfUserEntity.insertAndReturnId(_connection, user)
    _result
  }

  public override suspend fun insertSavedAddress(address: SavedAddressEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSavedAddressEntity.insertAndReturnId(_connection, address)
    _result
  }

  public override fun getAllCategories(): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM categories"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfImageResName: Int = getColumnIndexOrThrow(_stmt, "imageResName")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpImageResName: String
          _tmpImageResName = _stmt.getText(_columnIndexOfImageResName)
          val _tmpColorHex: String
          _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          _item = CategoryEntity(_tmpId,_tmpName,_tmpImageResName,_tmpColorHex)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllRestaurants(): Flow<List<RestaurantEntity>> {
    val _sql: String = "SELECT * FROM restaurants"
    return createFlow(__db, false, arrayOf("restaurants")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCuisine: Int = getColumnIndexOrThrow(_stmt, "cuisine")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfDeliveryTime: Int = getColumnIndexOrThrow(_stmt, "deliveryTime")
        val _columnIndexOfDeliveryFee: Int = getColumnIndexOrThrow(_stmt, "deliveryFee")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfBannerImage: Int = getColumnIndexOrThrow(_stmt, "bannerImage")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfIsVeg: Int = getColumnIndexOrThrow(_stmt, "isVeg")
        val _columnIndexOfIsPromoted: Int = getColumnIndexOrThrow(_stmt, "isPromoted")
        val _columnIndexOfDistanceKm: Int = getColumnIndexOrThrow(_stmt, "distanceKm")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _result: MutableList<RestaurantEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RestaurantEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCuisine: String
          _tmpCuisine = _stmt.getText(_columnIndexOfCuisine)
          val _tmpRating: Float
          _tmpRating = _stmt.getDouble(_columnIndexOfRating).toFloat()
          val _tmpDeliveryTime: Int
          _tmpDeliveryTime = _stmt.getLong(_columnIndexOfDeliveryTime).toInt()
          val _tmpDeliveryFee: Double
          _tmpDeliveryFee = _stmt.getDouble(_columnIndexOfDeliveryFee)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpBannerImage: String
          _tmpBannerImage = _stmt.getText(_columnIndexOfBannerImage)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpIsVeg: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVeg).toInt()
          _tmpIsVeg = _tmp != 0
          val _tmpIsPromoted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPromoted).toInt()
          _tmpIsPromoted = _tmp_1 != 0
          val _tmpDistanceKm: Float
          _tmpDistanceKm = _stmt.getDouble(_columnIndexOfDistanceKm).toFloat()
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          _item =
              RestaurantEntity(_tmpId,_tmpName,_tmpDescription,_tmpCuisine,_tmpRating,_tmpDeliveryTime,_tmpDeliveryFee,_tmpImage,_tmpBannerImage,_tmpAddress,_tmpIsVeg,_tmpIsPromoted,_tmpDistanceKm,_tmpLatitude,_tmpLongitude)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRestaurantById(id: Int): RestaurantEntity? {
    val _sql: String = "SELECT * FROM restaurants WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCuisine: Int = getColumnIndexOrThrow(_stmt, "cuisine")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfDeliveryTime: Int = getColumnIndexOrThrow(_stmt, "deliveryTime")
        val _columnIndexOfDeliveryFee: Int = getColumnIndexOrThrow(_stmt, "deliveryFee")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfBannerImage: Int = getColumnIndexOrThrow(_stmt, "bannerImage")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfIsVeg: Int = getColumnIndexOrThrow(_stmt, "isVeg")
        val _columnIndexOfIsPromoted: Int = getColumnIndexOrThrow(_stmt, "isPromoted")
        val _columnIndexOfDistanceKm: Int = getColumnIndexOrThrow(_stmt, "distanceKm")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _result: RestaurantEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpCuisine: String
          _tmpCuisine = _stmt.getText(_columnIndexOfCuisine)
          val _tmpRating: Float
          _tmpRating = _stmt.getDouble(_columnIndexOfRating).toFloat()
          val _tmpDeliveryTime: Int
          _tmpDeliveryTime = _stmt.getLong(_columnIndexOfDeliveryTime).toInt()
          val _tmpDeliveryFee: Double
          _tmpDeliveryFee = _stmt.getDouble(_columnIndexOfDeliveryFee)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpBannerImage: String
          _tmpBannerImage = _stmt.getText(_columnIndexOfBannerImage)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpIsVeg: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVeg).toInt()
          _tmpIsVeg = _tmp != 0
          val _tmpIsPromoted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPromoted).toInt()
          _tmpIsPromoted = _tmp_1 != 0
          val _tmpDistanceKm: Float
          _tmpDistanceKm = _stmt.getDouble(_columnIndexOfDistanceKm).toFloat()
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          _result =
              RestaurantEntity(_tmpId,_tmpName,_tmpDescription,_tmpCuisine,_tmpRating,_tmpDeliveryTime,_tmpDeliveryFee,_tmpImage,_tmpBannerImage,_tmpAddress,_tmpIsVeg,_tmpIsPromoted,_tmpDistanceKm,_tmpLatitude,_tmpLongitude)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getDishesForRestaurant(restaurantId: Int): Flow<List<DishEntity>> {
    val _sql: String = "SELECT * FROM dishes WHERE restaurantId = ?"
    return createFlow(__db, false, arrayOf("dishes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, restaurantId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsVeg: Int = getColumnIndexOrThrow(_stmt, "isVeg")
        val _columnIndexOfIsBestseller: Int = getColumnIndexOrThrow(_stmt, "isBestseller")
        val _columnIndexOfSpiceLevelSupport: Int = getColumnIndexOrThrow(_stmt, "spiceLevelSupport")
        val _columnIndexOfAddonsJson: Int = getColumnIndexOrThrow(_stmt, "addonsJson")
        val _result: MutableList<DishEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DishEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsVeg: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVeg).toInt()
          _tmpIsVeg = _tmp != 0
          val _tmpIsBestseller: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsBestseller).toInt()
          _tmpIsBestseller = _tmp_1 != 0
          val _tmpSpiceLevelSupport: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfSpiceLevelSupport).toInt()
          _tmpSpiceLevelSupport = _tmp_2 != 0
          val _tmpAddonsJson: String
          _tmpAddonsJson = _stmt.getText(_columnIndexOfAddonsJson)
          _item =
              DishEntity(_tmpId,_tmpRestaurantId,_tmpName,_tmpPrice,_tmpDescription,_tmpImage,_tmpCategory,_tmpIsVeg,_tmpIsBestseller,_tmpSpiceLevelSupport,_tmpAddonsJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllDishes(): Flow<List<DishEntity>> {
    val _sql: String = "SELECT * FROM dishes"
    return createFlow(__db, false, arrayOf("dishes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsVeg: Int = getColumnIndexOrThrow(_stmt, "isVeg")
        val _columnIndexOfIsBestseller: Int = getColumnIndexOrThrow(_stmt, "isBestseller")
        val _columnIndexOfSpiceLevelSupport: Int = getColumnIndexOrThrow(_stmt, "spiceLevelSupport")
        val _columnIndexOfAddonsJson: Int = getColumnIndexOrThrow(_stmt, "addonsJson")
        val _result: MutableList<DishEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DishEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsVeg: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVeg).toInt()
          _tmpIsVeg = _tmp != 0
          val _tmpIsBestseller: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsBestseller).toInt()
          _tmpIsBestseller = _tmp_1 != 0
          val _tmpSpiceLevelSupport: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfSpiceLevelSupport).toInt()
          _tmpSpiceLevelSupport = _tmp_2 != 0
          val _tmpAddonsJson: String
          _tmpAddonsJson = _stmt.getText(_columnIndexOfAddonsJson)
          _item =
              DishEntity(_tmpId,_tmpRestaurantId,_tmpName,_tmpPrice,_tmpDescription,_tmpImage,_tmpCategory,_tmpIsVeg,_tmpIsBestseller,_tmpSpiceLevelSupport,_tmpAddonsJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDishById(id: Int): DishEntity? {
    val _sql: String = "SELECT * FROM dishes WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfImage: Int = getColumnIndexOrThrow(_stmt, "image")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsVeg: Int = getColumnIndexOrThrow(_stmt, "isVeg")
        val _columnIndexOfIsBestseller: Int = getColumnIndexOrThrow(_stmt, "isBestseller")
        val _columnIndexOfSpiceLevelSupport: Int = getColumnIndexOrThrow(_stmt, "spiceLevelSupport")
        val _columnIndexOfAddonsJson: Int = getColumnIndexOrThrow(_stmt, "addonsJson")
        val _result: DishEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpImage: String
          _tmpImage = _stmt.getText(_columnIndexOfImage)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsVeg: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVeg).toInt()
          _tmpIsVeg = _tmp != 0
          val _tmpIsBestseller: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsBestseller).toInt()
          _tmpIsBestseller = _tmp_1 != 0
          val _tmpSpiceLevelSupport: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfSpiceLevelSupport).toInt()
          _tmpSpiceLevelSupport = _tmp_2 != 0
          val _tmpAddonsJson: String
          _tmpAddonsJson = _stmt.getText(_columnIndexOfAddonsJson)
          _result =
              DishEntity(_tmpId,_tmpRestaurantId,_tmpName,_tmpPrice,_tmpDescription,_tmpImage,_tmpCategory,_tmpIsVeg,_tmpIsBestseller,_tmpSpiceLevelSupport,_tmpAddonsJson)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCartItems(): Flow<List<CartItemEntity>> {
    val _sql: String = "SELECT * FROM cart_items"
    return createFlow(__db, false, arrayOf("cart_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDishId: Int = getColumnIndexOrThrow(_stmt, "dishId")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfRestaurantName: Int = getColumnIndexOrThrow(_stmt, "restaurantName")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfSpiceLevel: Int = getColumnIndexOrThrow(_stmt, "spiceLevel")
        val _columnIndexOfAddons: Int = getColumnIndexOrThrow(_stmt, "addons")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<CartItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CartItemEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDishId: Int
          _tmpDishId = _stmt.getLong(_columnIndexOfDishId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpRestaurantName: String
          _tmpRestaurantName = _stmt.getText(_columnIndexOfRestaurantName)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          val _tmpSpiceLevel: String
          _tmpSpiceLevel = _stmt.getText(_columnIndexOfSpiceLevel)
          val _tmpAddons: String
          _tmpAddons = _stmt.getText(_columnIndexOfAddons)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item =
              CartItemEntity(_tmpId,_tmpDishId,_tmpRestaurantId,_tmpRestaurantName,_tmpName,_tmpPrice,_tmpQuantity,_tmpSpiceLevel,_tmpAddons,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllOrders(): Flow<List<OrderEntity>> {
    val _sql: String = "SELECT * FROM orders ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("orders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfRestaurantName: Int = getColumnIndexOrThrow(_stmt, "restaurantName")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfTotalAmount: Int = getColumnIndexOrThrow(_stmt, "totalAmount")
        val _columnIndexOfItemsSummary: Int = getColumnIndexOrThrow(_stmt, "itemsSummary")
        val _columnIndexOfItemsDetailJson: Int = getColumnIndexOrThrow(_stmt, "itemsDetailJson")
        val _columnIndexOfPaymentMethod: Int = getColumnIndexOrThrow(_stmt, "paymentMethod")
        val _columnIndexOfDeliveryAddress: Int = getColumnIndexOrThrow(_stmt, "deliveryAddress")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDriverName: Int = getColumnIndexOrThrow(_stmt, "driverName")
        val _columnIndexOfDriverPhone: Int = getColumnIndexOrThrow(_stmt, "driverPhone")
        val _columnIndexOfDriverLat: Int = getColumnIndexOrThrow(_stmt, "driverLat")
        val _columnIndexOfDriverLng: Int = getColumnIndexOrThrow(_stmt, "driverLng")
        val _columnIndexOfCustomerLat: Int = getColumnIndexOrThrow(_stmt, "customerLat")
        val _columnIndexOfCustomerLng: Int = getColumnIndexOrThrow(_stmt, "customerLng")
        val _columnIndexOfRestaurantLat: Int = getColumnIndexOrThrow(_stmt, "restaurantLat")
        val _columnIndexOfRestaurantLng: Int = getColumnIndexOrThrow(_stmt, "restaurantLng")
        val _columnIndexOfRatingGiven: Int = getColumnIndexOrThrow(_stmt, "ratingGiven")
        val _columnIndexOfReviewText: Int = getColumnIndexOrThrow(_stmt, "reviewText")
        val _columnIndexOfReviewSentiment: Int = getColumnIndexOrThrow(_stmt, "reviewSentiment")
        val _result: MutableList<OrderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: OrderEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpRestaurantName: String
          _tmpRestaurantName = _stmt.getText(_columnIndexOfRestaurantName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpTotalAmount: Double
          _tmpTotalAmount = _stmt.getDouble(_columnIndexOfTotalAmount)
          val _tmpItemsSummary: String
          _tmpItemsSummary = _stmt.getText(_columnIndexOfItemsSummary)
          val _tmpItemsDetailJson: String
          _tmpItemsDetailJson = _stmt.getText(_columnIndexOfItemsDetailJson)
          val _tmpPaymentMethod: String
          _tmpPaymentMethod = _stmt.getText(_columnIndexOfPaymentMethod)
          val _tmpDeliveryAddress: String
          _tmpDeliveryAddress = _stmt.getText(_columnIndexOfDeliveryAddress)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDriverName: String
          _tmpDriverName = _stmt.getText(_columnIndexOfDriverName)
          val _tmpDriverPhone: String
          _tmpDriverPhone = _stmt.getText(_columnIndexOfDriverPhone)
          val _tmpDriverLat: Double
          _tmpDriverLat = _stmt.getDouble(_columnIndexOfDriverLat)
          val _tmpDriverLng: Double
          _tmpDriverLng = _stmt.getDouble(_columnIndexOfDriverLng)
          val _tmpCustomerLat: Double
          _tmpCustomerLat = _stmt.getDouble(_columnIndexOfCustomerLat)
          val _tmpCustomerLng: Double
          _tmpCustomerLng = _stmt.getDouble(_columnIndexOfCustomerLng)
          val _tmpRestaurantLat: Double
          _tmpRestaurantLat = _stmt.getDouble(_columnIndexOfRestaurantLat)
          val _tmpRestaurantLng: Double
          _tmpRestaurantLng = _stmt.getDouble(_columnIndexOfRestaurantLng)
          val _tmpRatingGiven: Float
          _tmpRatingGiven = _stmt.getDouble(_columnIndexOfRatingGiven).toFloat()
          val _tmpReviewText: String
          _tmpReviewText = _stmt.getText(_columnIndexOfReviewText)
          val _tmpReviewSentiment: String
          _tmpReviewSentiment = _stmt.getText(_columnIndexOfReviewSentiment)
          _item =
              OrderEntity(_tmpId,_tmpRestaurantId,_tmpRestaurantName,_tmpStatus,_tmpTotalAmount,_tmpItemsSummary,_tmpItemsDetailJson,_tmpPaymentMethod,_tmpDeliveryAddress,_tmpTimestamp,_tmpDriverName,_tmpDriverPhone,_tmpDriverLat,_tmpDriverLng,_tmpCustomerLat,_tmpCustomerLng,_tmpRestaurantLat,_tmpRestaurantLng,_tmpRatingGiven,_tmpReviewText,_tmpReviewSentiment)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getOrderFlowById(id: Int): Flow<OrderEntity?> {
    val _sql: String = "SELECT * FROM orders WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("orders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfRestaurantName: Int = getColumnIndexOrThrow(_stmt, "restaurantName")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfTotalAmount: Int = getColumnIndexOrThrow(_stmt, "totalAmount")
        val _columnIndexOfItemsSummary: Int = getColumnIndexOrThrow(_stmt, "itemsSummary")
        val _columnIndexOfItemsDetailJson: Int = getColumnIndexOrThrow(_stmt, "itemsDetailJson")
        val _columnIndexOfPaymentMethod: Int = getColumnIndexOrThrow(_stmt, "paymentMethod")
        val _columnIndexOfDeliveryAddress: Int = getColumnIndexOrThrow(_stmt, "deliveryAddress")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDriverName: Int = getColumnIndexOrThrow(_stmt, "driverName")
        val _columnIndexOfDriverPhone: Int = getColumnIndexOrThrow(_stmt, "driverPhone")
        val _columnIndexOfDriverLat: Int = getColumnIndexOrThrow(_stmt, "driverLat")
        val _columnIndexOfDriverLng: Int = getColumnIndexOrThrow(_stmt, "driverLng")
        val _columnIndexOfCustomerLat: Int = getColumnIndexOrThrow(_stmt, "customerLat")
        val _columnIndexOfCustomerLng: Int = getColumnIndexOrThrow(_stmt, "customerLng")
        val _columnIndexOfRestaurantLat: Int = getColumnIndexOrThrow(_stmt, "restaurantLat")
        val _columnIndexOfRestaurantLng: Int = getColumnIndexOrThrow(_stmt, "restaurantLng")
        val _columnIndexOfRatingGiven: Int = getColumnIndexOrThrow(_stmt, "ratingGiven")
        val _columnIndexOfReviewText: Int = getColumnIndexOrThrow(_stmt, "reviewText")
        val _columnIndexOfReviewSentiment: Int = getColumnIndexOrThrow(_stmt, "reviewSentiment")
        val _result: OrderEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpRestaurantName: String
          _tmpRestaurantName = _stmt.getText(_columnIndexOfRestaurantName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpTotalAmount: Double
          _tmpTotalAmount = _stmt.getDouble(_columnIndexOfTotalAmount)
          val _tmpItemsSummary: String
          _tmpItemsSummary = _stmt.getText(_columnIndexOfItemsSummary)
          val _tmpItemsDetailJson: String
          _tmpItemsDetailJson = _stmt.getText(_columnIndexOfItemsDetailJson)
          val _tmpPaymentMethod: String
          _tmpPaymentMethod = _stmt.getText(_columnIndexOfPaymentMethod)
          val _tmpDeliveryAddress: String
          _tmpDeliveryAddress = _stmt.getText(_columnIndexOfDeliveryAddress)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDriverName: String
          _tmpDriverName = _stmt.getText(_columnIndexOfDriverName)
          val _tmpDriverPhone: String
          _tmpDriverPhone = _stmt.getText(_columnIndexOfDriverPhone)
          val _tmpDriverLat: Double
          _tmpDriverLat = _stmt.getDouble(_columnIndexOfDriverLat)
          val _tmpDriverLng: Double
          _tmpDriverLng = _stmt.getDouble(_columnIndexOfDriverLng)
          val _tmpCustomerLat: Double
          _tmpCustomerLat = _stmt.getDouble(_columnIndexOfCustomerLat)
          val _tmpCustomerLng: Double
          _tmpCustomerLng = _stmt.getDouble(_columnIndexOfCustomerLng)
          val _tmpRestaurantLat: Double
          _tmpRestaurantLat = _stmt.getDouble(_columnIndexOfRestaurantLat)
          val _tmpRestaurantLng: Double
          _tmpRestaurantLng = _stmt.getDouble(_columnIndexOfRestaurantLng)
          val _tmpRatingGiven: Float
          _tmpRatingGiven = _stmt.getDouble(_columnIndexOfRatingGiven).toFloat()
          val _tmpReviewText: String
          _tmpReviewText = _stmt.getText(_columnIndexOfReviewText)
          val _tmpReviewSentiment: String
          _tmpReviewSentiment = _stmt.getText(_columnIndexOfReviewSentiment)
          _result =
              OrderEntity(_tmpId,_tmpRestaurantId,_tmpRestaurantName,_tmpStatus,_tmpTotalAmount,_tmpItemsSummary,_tmpItemsDetailJson,_tmpPaymentMethod,_tmpDeliveryAddress,_tmpTimestamp,_tmpDriverName,_tmpDriverPhone,_tmpDriverLat,_tmpDriverLng,_tmpCustomerLat,_tmpCustomerLng,_tmpRestaurantLat,_tmpRestaurantLng,_tmpRatingGiven,_tmpReviewText,_tmpReviewSentiment)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getOrderById(id: Int): OrderEntity? {
    val _sql: String = "SELECT * FROM orders WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRestaurantId: Int = getColumnIndexOrThrow(_stmt, "restaurantId")
        val _columnIndexOfRestaurantName: Int = getColumnIndexOrThrow(_stmt, "restaurantName")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfTotalAmount: Int = getColumnIndexOrThrow(_stmt, "totalAmount")
        val _columnIndexOfItemsSummary: Int = getColumnIndexOrThrow(_stmt, "itemsSummary")
        val _columnIndexOfItemsDetailJson: Int = getColumnIndexOrThrow(_stmt, "itemsDetailJson")
        val _columnIndexOfPaymentMethod: Int = getColumnIndexOrThrow(_stmt, "paymentMethod")
        val _columnIndexOfDeliveryAddress: Int = getColumnIndexOrThrow(_stmt, "deliveryAddress")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDriverName: Int = getColumnIndexOrThrow(_stmt, "driverName")
        val _columnIndexOfDriverPhone: Int = getColumnIndexOrThrow(_stmt, "driverPhone")
        val _columnIndexOfDriverLat: Int = getColumnIndexOrThrow(_stmt, "driverLat")
        val _columnIndexOfDriverLng: Int = getColumnIndexOrThrow(_stmt, "driverLng")
        val _columnIndexOfCustomerLat: Int = getColumnIndexOrThrow(_stmt, "customerLat")
        val _columnIndexOfCustomerLng: Int = getColumnIndexOrThrow(_stmt, "customerLng")
        val _columnIndexOfRestaurantLat: Int = getColumnIndexOrThrow(_stmt, "restaurantLat")
        val _columnIndexOfRestaurantLng: Int = getColumnIndexOrThrow(_stmt, "restaurantLng")
        val _columnIndexOfRatingGiven: Int = getColumnIndexOrThrow(_stmt, "ratingGiven")
        val _columnIndexOfReviewText: Int = getColumnIndexOrThrow(_stmt, "reviewText")
        val _columnIndexOfReviewSentiment: Int = getColumnIndexOrThrow(_stmt, "reviewSentiment")
        val _result: OrderEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRestaurantId: Int
          _tmpRestaurantId = _stmt.getLong(_columnIndexOfRestaurantId).toInt()
          val _tmpRestaurantName: String
          _tmpRestaurantName = _stmt.getText(_columnIndexOfRestaurantName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpTotalAmount: Double
          _tmpTotalAmount = _stmt.getDouble(_columnIndexOfTotalAmount)
          val _tmpItemsSummary: String
          _tmpItemsSummary = _stmt.getText(_columnIndexOfItemsSummary)
          val _tmpItemsDetailJson: String
          _tmpItemsDetailJson = _stmt.getText(_columnIndexOfItemsDetailJson)
          val _tmpPaymentMethod: String
          _tmpPaymentMethod = _stmt.getText(_columnIndexOfPaymentMethod)
          val _tmpDeliveryAddress: String
          _tmpDeliveryAddress = _stmt.getText(_columnIndexOfDeliveryAddress)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDriverName: String
          _tmpDriverName = _stmt.getText(_columnIndexOfDriverName)
          val _tmpDriverPhone: String
          _tmpDriverPhone = _stmt.getText(_columnIndexOfDriverPhone)
          val _tmpDriverLat: Double
          _tmpDriverLat = _stmt.getDouble(_columnIndexOfDriverLat)
          val _tmpDriverLng: Double
          _tmpDriverLng = _stmt.getDouble(_columnIndexOfDriverLng)
          val _tmpCustomerLat: Double
          _tmpCustomerLat = _stmt.getDouble(_columnIndexOfCustomerLat)
          val _tmpCustomerLng: Double
          _tmpCustomerLng = _stmt.getDouble(_columnIndexOfCustomerLng)
          val _tmpRestaurantLat: Double
          _tmpRestaurantLat = _stmt.getDouble(_columnIndexOfRestaurantLat)
          val _tmpRestaurantLng: Double
          _tmpRestaurantLng = _stmt.getDouble(_columnIndexOfRestaurantLng)
          val _tmpRatingGiven: Float
          _tmpRatingGiven = _stmt.getDouble(_columnIndexOfRatingGiven).toFloat()
          val _tmpReviewText: String
          _tmpReviewText = _stmt.getText(_columnIndexOfReviewText)
          val _tmpReviewSentiment: String
          _tmpReviewSentiment = _stmt.getText(_columnIndexOfReviewSentiment)
          _result =
              OrderEntity(_tmpId,_tmpRestaurantId,_tmpRestaurantName,_tmpStatus,_tmpTotalAmount,_tmpItemsSummary,_tmpItemsDetailJson,_tmpPaymentMethod,_tmpDeliveryAddress,_tmpTimestamp,_tmpDriverName,_tmpDriverPhone,_tmpDriverLat,_tmpDriverLng,_tmpCustomerLat,_tmpCustomerLng,_tmpRestaurantLat,_tmpRestaurantLng,_tmpRatingGiven,_tmpReviewText,_tmpReviewSentiment)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getWalletTransactions(): Flow<List<WalletTransactionEntity>> {
    val _sql: String = "SELECT * FROM wallet_transactions ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("wallet_transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfMemberName: Int = getColumnIndexOrThrow(_stmt, "memberName")
        val _result: MutableList<WalletTransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WalletTransactionEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpMemberName: String
          _tmpMemberName = _stmt.getText(_columnIndexOfMemberName)
          _item =
              WalletTransactionEntity(_tmpId,_tmpType,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpMemberName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getChatMessages(): Flow<List<ChatMessageEntity>> {
    val _sql: String = "SELECT * FROM chat_messages ORDER BY timestamp ASC"
    return createFlow(__db, false, arrayOf("chat_messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSender: Int = getColumnIndexOrThrow(_stmt, "sender")
        val _columnIndexOfMessage: Int = getColumnIndexOrThrow(_stmt, "message")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<ChatMessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatMessageEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpSender: String
          _tmpSender = _stmt.getText(_columnIndexOfSender)
          val _tmpMessage: String
          _tmpMessage = _stmt.getText(_columnIndexOfMessage)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = ChatMessageEntity(_tmpId,_tmpSender,_tmpMessage,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getFamilyMembers(): Flow<List<FamilyMemberEntity>> {
    val _sql: String =
        "SELECT * FROM family_members WHERE isActive = 1 ORDER BY isAdmin DESC, name ASC"
    return createFlow(__db, false, arrayOf("family_members")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAvatarColor: Int = getColumnIndexOrThrow(_stmt, "avatarColor")
        val _columnIndexOfSpendingLimit: Int = getColumnIndexOrThrow(_stmt, "spendingLimit")
        val _columnIndexOfMonthlySpent: Int = getColumnIndexOrThrow(_stmt, "monthlySpent")
        val _columnIndexOfIsAdmin: Int = getColumnIndexOrThrow(_stmt, "isAdmin")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _result: MutableList<FamilyMemberEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FamilyMemberEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAvatarColor: Long
          _tmpAvatarColor = _stmt.getLong(_columnIndexOfAvatarColor)
          val _tmpSpendingLimit: Double
          _tmpSpendingLimit = _stmt.getDouble(_columnIndexOfSpendingLimit)
          val _tmpMonthlySpent: Double
          _tmpMonthlySpent = _stmt.getDouble(_columnIndexOfMonthlySpent)
          val _tmpIsAdmin: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsAdmin).toInt()
          _tmpIsAdmin = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          _item =
              FamilyMemberEntity(_tmpId,_tmpName,_tmpEmail,_tmpAvatarColor,_tmpSpendingLimit,_tmpMonthlySpent,_tmpIsAdmin,_tmpIsActive,_tmpJoinedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getFamilyMemberCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM family_members WHERE isActive = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalFamilySpending(): Double? {
    val _sql: String = "SELECT SUM(monthlySpent) FROM family_members WHERE isActive = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getFamilyTransactions(): Flow<List<FamilyTransactionEntity>> {
    val _sql: String = "SELECT * FROM family_transactions ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("family_transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMemberId: Int = getColumnIndexOrThrow(_stmt, "memberId")
        val _columnIndexOfMemberName: Int = getColumnIndexOrThrow(_stmt, "memberName")
        val _columnIndexOfOrderId: Int = getColumnIndexOrThrow(_stmt, "orderId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FamilyTransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FamilyTransactionEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMemberId: Int
          _tmpMemberId = _stmt.getLong(_columnIndexOfMemberId).toInt()
          val _tmpMemberName: String
          _tmpMemberName = _stmt.getText(_columnIndexOfMemberName)
          val _tmpOrderId: Int
          _tmpOrderId = _stmt.getLong(_columnIndexOfOrderId).toInt()
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item =
              FamilyTransactionEntity(_tmpId,_tmpMemberId,_tmpMemberName,_tmpOrderId,_tmpAmount,_tmpDescription,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTransactionsForMember(memberId: Int): Flow<List<FamilyTransactionEntity>> {
    val _sql: String =
        "SELECT * FROM family_transactions WHERE memberId = ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("family_transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, memberId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMemberId: Int = getColumnIndexOrThrow(_stmt, "memberId")
        val _columnIndexOfMemberName: Int = getColumnIndexOrThrow(_stmt, "memberName")
        val _columnIndexOfOrderId: Int = getColumnIndexOrThrow(_stmt, "orderId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FamilyTransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FamilyTransactionEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMemberId: Int
          _tmpMemberId = _stmt.getLong(_columnIndexOfMemberId).toInt()
          val _tmpMemberName: String
          _tmpMemberName = _stmt.getText(_columnIndexOfMemberName)
          val _tmpOrderId: Int
          _tmpOrderId = _stmt.getLong(_columnIndexOfOrderId).toInt()
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item =
              FamilyTransactionEntity(_tmpId,_tmpMemberId,_tmpMemberName,_tmpOrderId,_tmpAmount,_tmpDescription,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPaymentMethods(): Flow<List<PaymentMethodEntity>> {
    val _sql: String = "SELECT * FROM payment_methods ORDER BY isDefault DESC, addedAt DESC"
    return createFlow(__db, false, arrayOf("payment_methods")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfMaskedValue: Int = getColumnIndexOrThrow(_stmt, "maskedValue")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _result: MutableList<PaymentMethodEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PaymentMethodEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpMaskedValue: String
          _tmpMaskedValue = _stmt.getText(_columnIndexOfMaskedValue)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          _item =
              PaymentMethodEntity(_tmpId,_tmpType,_tmpLabel,_tmpMaskedValue,_tmpIsDefault,_tmpAddedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserByPhone(phone: String): UserEntity? {
    val _sql: String = "SELECT * FROM users WHERE phone = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, phone)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAvatarUrl: Int = getColumnIndexOrThrow(_stmt, "avatarUrl")
        val _columnIndexOfSessionToken: Int = getColumnIndexOrThrow(_stmt, "sessionToken")
        val _columnIndexOfIsVerified: Int = getColumnIndexOrThrow(_stmt, "isVerified")
        val _columnIndexOfDefaultAddressId: Int = getColumnIndexOrThrow(_stmt, "defaultAddressId")
        val _columnIndexOfIsGoldMember: Int = getColumnIndexOrThrow(_stmt, "isGoldMember")
        val _columnIndexOfWalletBalance: Int = getColumnIndexOrThrow(_stmt, "walletBalance")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastLoginAt: Int = getColumnIndexOrThrow(_stmt, "lastLoginAt")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAvatarUrl: String
          _tmpAvatarUrl = _stmt.getText(_columnIndexOfAvatarUrl)
          val _tmpSessionToken: String
          _tmpSessionToken = _stmt.getText(_columnIndexOfSessionToken)
          val _tmpIsVerified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVerified).toInt()
          _tmpIsVerified = _tmp != 0
          val _tmpDefaultAddressId: Int
          _tmpDefaultAddressId = _stmt.getLong(_columnIndexOfDefaultAddressId).toInt()
          val _tmpIsGoldMember: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsGoldMember).toInt()
          _tmpIsGoldMember = _tmp_1 != 0
          val _tmpWalletBalance: Double
          _tmpWalletBalance = _stmt.getDouble(_columnIndexOfWalletBalance)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastLoginAt: Long
          _tmpLastLoginAt = _stmt.getLong(_columnIndexOfLastLoginAt)
          _result =
              UserEntity(_tmpId,_tmpPhone,_tmpName,_tmpEmail,_tmpAvatarUrl,_tmpSessionToken,_tmpIsVerified,_tmpDefaultAddressId,_tmpIsGoldMember,_tmpWalletBalance,_tmpCreatedAt,_tmpLastLoginAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserById(id: Int): UserEntity? {
    val _sql: String = "SELECT * FROM users WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAvatarUrl: Int = getColumnIndexOrThrow(_stmt, "avatarUrl")
        val _columnIndexOfSessionToken: Int = getColumnIndexOrThrow(_stmt, "sessionToken")
        val _columnIndexOfIsVerified: Int = getColumnIndexOrThrow(_stmt, "isVerified")
        val _columnIndexOfDefaultAddressId: Int = getColumnIndexOrThrow(_stmt, "defaultAddressId")
        val _columnIndexOfIsGoldMember: Int = getColumnIndexOrThrow(_stmt, "isGoldMember")
        val _columnIndexOfWalletBalance: Int = getColumnIndexOrThrow(_stmt, "walletBalance")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastLoginAt: Int = getColumnIndexOrThrow(_stmt, "lastLoginAt")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAvatarUrl: String
          _tmpAvatarUrl = _stmt.getText(_columnIndexOfAvatarUrl)
          val _tmpSessionToken: String
          _tmpSessionToken = _stmt.getText(_columnIndexOfSessionToken)
          val _tmpIsVerified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVerified).toInt()
          _tmpIsVerified = _tmp != 0
          val _tmpDefaultAddressId: Int
          _tmpDefaultAddressId = _stmt.getLong(_columnIndexOfDefaultAddressId).toInt()
          val _tmpIsGoldMember: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsGoldMember).toInt()
          _tmpIsGoldMember = _tmp_1 != 0
          val _tmpWalletBalance: Double
          _tmpWalletBalance = _stmt.getDouble(_columnIndexOfWalletBalance)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastLoginAt: Long
          _tmpLastLoginAt = _stmt.getLong(_columnIndexOfLastLoginAt)
          _result =
              UserEntity(_tmpId,_tmpPhone,_tmpName,_tmpEmail,_tmpAvatarUrl,_tmpSessionToken,_tmpIsVerified,_tmpDefaultAddressId,_tmpIsGoldMember,_tmpWalletBalance,_tmpCreatedAt,_tmpLastLoginAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCurrentUser(): UserEntity? {
    val _sql: String = "SELECT * FROM users LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAvatarUrl: Int = getColumnIndexOrThrow(_stmt, "avatarUrl")
        val _columnIndexOfSessionToken: Int = getColumnIndexOrThrow(_stmt, "sessionToken")
        val _columnIndexOfIsVerified: Int = getColumnIndexOrThrow(_stmt, "isVerified")
        val _columnIndexOfDefaultAddressId: Int = getColumnIndexOrThrow(_stmt, "defaultAddressId")
        val _columnIndexOfIsGoldMember: Int = getColumnIndexOrThrow(_stmt, "isGoldMember")
        val _columnIndexOfWalletBalance: Int = getColumnIndexOrThrow(_stmt, "walletBalance")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastLoginAt: Int = getColumnIndexOrThrow(_stmt, "lastLoginAt")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAvatarUrl: String
          _tmpAvatarUrl = _stmt.getText(_columnIndexOfAvatarUrl)
          val _tmpSessionToken: String
          _tmpSessionToken = _stmt.getText(_columnIndexOfSessionToken)
          val _tmpIsVerified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVerified).toInt()
          _tmpIsVerified = _tmp != 0
          val _tmpDefaultAddressId: Int
          _tmpDefaultAddressId = _stmt.getLong(_columnIndexOfDefaultAddressId).toInt()
          val _tmpIsGoldMember: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsGoldMember).toInt()
          _tmpIsGoldMember = _tmp_1 != 0
          val _tmpWalletBalance: Double
          _tmpWalletBalance = _stmt.getDouble(_columnIndexOfWalletBalance)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastLoginAt: Long
          _tmpLastLoginAt = _stmt.getLong(_columnIndexOfLastLoginAt)
          _result =
              UserEntity(_tmpId,_tmpPhone,_tmpName,_tmpEmail,_tmpAvatarUrl,_tmpSessionToken,_tmpIsVerified,_tmpDefaultAddressId,_tmpIsGoldMember,_tmpWalletBalance,_tmpCreatedAt,_tmpLastLoginAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeCurrentUser(): Flow<UserEntity?> {
    val _sql: String = "SELECT * FROM users LIMIT 1"
    return createFlow(__db, false, arrayOf("users")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAvatarUrl: Int = getColumnIndexOrThrow(_stmt, "avatarUrl")
        val _columnIndexOfSessionToken: Int = getColumnIndexOrThrow(_stmt, "sessionToken")
        val _columnIndexOfIsVerified: Int = getColumnIndexOrThrow(_stmt, "isVerified")
        val _columnIndexOfDefaultAddressId: Int = getColumnIndexOrThrow(_stmt, "defaultAddressId")
        val _columnIndexOfIsGoldMember: Int = getColumnIndexOrThrow(_stmt, "isGoldMember")
        val _columnIndexOfWalletBalance: Int = getColumnIndexOrThrow(_stmt, "walletBalance")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastLoginAt: Int = getColumnIndexOrThrow(_stmt, "lastLoginAt")
        val _result: UserEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAvatarUrl: String
          _tmpAvatarUrl = _stmt.getText(_columnIndexOfAvatarUrl)
          val _tmpSessionToken: String
          _tmpSessionToken = _stmt.getText(_columnIndexOfSessionToken)
          val _tmpIsVerified: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsVerified).toInt()
          _tmpIsVerified = _tmp != 0
          val _tmpDefaultAddressId: Int
          _tmpDefaultAddressId = _stmt.getLong(_columnIndexOfDefaultAddressId).toInt()
          val _tmpIsGoldMember: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsGoldMember).toInt()
          _tmpIsGoldMember = _tmp_1 != 0
          val _tmpWalletBalance: Double
          _tmpWalletBalance = _stmt.getDouble(_columnIndexOfWalletBalance)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastLoginAt: Long
          _tmpLastLoginAt = _stmt.getLong(_columnIndexOfLastLoginAt)
          _result =
              UserEntity(_tmpId,_tmpPhone,_tmpName,_tmpEmail,_tmpAvatarUrl,_tmpSessionToken,_tmpIsVerified,_tmpDefaultAddressId,_tmpIsGoldMember,_tmpWalletBalance,_tmpCreatedAt,_tmpLastLoginAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSavedAddresses(userId: Int): Flow<List<SavedAddressEntity>> {
    val _sql: String =
        "SELECT * FROM saved_addresses WHERE userId = ? ORDER BY isDefault DESC, createdAt DESC"
    return createFlow(__db, false, arrayOf("saved_addresses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, userId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfFullAddress: Int = getColumnIndexOrThrow(_stmt, "fullAddress")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SavedAddressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavedAddressEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserId: Int
          _tmpUserId = _stmt.getLong(_columnIndexOfUserId).toInt()
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpFullAddress: String
          _tmpFullAddress = _stmt.getText(_columnIndexOfFullAddress)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              SavedAddressEntity(_tmpId,_tmpUserId,_tmpLabel,_tmpFullAddress,_tmpLatitude,_tmpLongitude,_tmpIsDefault,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDefaultAddress(userId: Int): SavedAddressEntity? {
    val _sql: String = "SELECT * FROM saved_addresses WHERE userId = ? AND isDefault = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, userId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfFullAddress: Int = getColumnIndexOrThrow(_stmt, "fullAddress")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: SavedAddressEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserId: Int
          _tmpUserId = _stmt.getLong(_columnIndexOfUserId).toInt()
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpFullAddress: String
          _tmpFullAddress = _stmt.getText(_columnIndexOfFullAddress)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              SavedAddressEntity(_tmpId,_tmpUserId,_tmpLabel,_tmpFullAddress,_tmpLatitude,_tmpLongitude,_tmpIsDefault,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearCategories() {
    val _sql: String = "DELETE FROM categories"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearRestaurants() {
    val _sql: String = "DELETE FROM restaurants"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearDishesForRestaurant(restaurantId: Int) {
    val _sql: String = "DELETE FROM dishes WHERE restaurantId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, restaurantId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCartQuantity(id: Int, quantity: Int) {
    val _sql: String = "UPDATE cart_items SET quantity = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, quantity.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCartItem(id: Int) {
    val _sql: String = "DELETE FROM cart_items WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearCart() {
    val _sql: String = "DELETE FROM cart_items"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateOrderStatus(id: Int, status: String) {
    val _sql: String = "UPDATE orders SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateDriverLocation(
    id: Int,
    lat: Double,
    lng: Double,
  ) {
    val _sql: String = "UPDATE orders SET driverLat = ?, driverLng = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, lat)
        _argIndex = 2
        _stmt.bindDouble(_argIndex, lng)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun submitOrderReview(
    id: Int,
    rating: Float,
    review: String,
    sentiment: String,
  ) {
    val _sql: String =
        "UPDATE orders SET ratingGiven = ?, reviewText = ?, reviewSentiment = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, rating.toDouble())
        _argIndex = 2
        _stmt.bindText(_argIndex, review)
        _argIndex = 3
        _stmt.bindText(_argIndex, sentiment)
        _argIndex = 4
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearWalletTransactions() {
    val _sql: String = "DELETE FROM wallet_transactions"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearChatMessages() {
    val _sql: String = "DELETE FROM chat_messages"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateMemberSpendingLimit(id: Int, limit: Double) {
    val _sql: String = "UPDATE family_members SET spendingLimit = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, limit)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementMemberSpending(id: Int, amount: Double) {
    val _sql: String = "UPDATE family_members SET monthlySpent = monthlySpent + ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, amount)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deactivateMember(id: Int) {
    val _sql: String = "UPDATE family_members SET isActive = 0 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePaymentMethod(id: Int) {
    val _sql: String = "DELETE FROM payment_methods WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearDefaultPaymentMethod() {
    val _sql: String = "UPDATE payment_methods SET isDefault = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setDefaultPaymentMethod(id: Int) {
    val _sql: String = "UPDATE payment_methods SET isDefault = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearUsers() {
    val _sql: String = "DELETE FROM users"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateUserProfile(
    phone: String,
    name: String,
    email: String,
  ) {
    val _sql: String = "UPDATE users SET name = ?, email = ? WHERE phone = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _argIndex = 2
        _stmt.bindText(_argIndex, email)
        _argIndex = 3
        _stmt.bindText(_argIndex, phone)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateUserSession(
    phone: String,
    token: String,
    ts: Long,
  ) {
    val _sql: String = "UPDATE users SET lastLoginAt = ?, sessionToken = ? WHERE phone = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, ts)
        _argIndex = 2
        _stmt.bindText(_argIndex, token)
        _argIndex = 3
        _stmt.bindText(_argIndex, phone)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markUserVerified(phone: String) {
    val _sql: String = "UPDATE users SET isVerified = 1 WHERE phone = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, phone)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateDefaultAddress(phone: String, addressId: Int) {
    val _sql: String = "UPDATE users SET defaultAddressId = ? WHERE phone = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, addressId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, phone)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearDefaultAddress(userId: Int) {
    val _sql: String = "UPDATE saved_addresses SET isDefault = 0 WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, userId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setDefaultAddress(addressId: Int) {
    val _sql: String = "UPDATE saved_addresses SET isDefault = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, addressId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAddress(id: Int) {
    val _sql: String = "DELETE FROM saved_addresses WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearSavedAddresses(userId: Int) {
    val _sql: String = "DELETE FROM saved_addresses WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, userId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
