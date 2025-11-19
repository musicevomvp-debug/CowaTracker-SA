package com.cowatracker.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY notificationTime DESC")
    fun getAllOrders(): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): Order?

    @Query("SELECT * FROM orders WHERE date(notificationTime/1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY notificationTime DESC")
    fun getTodayOrders(): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE date(notificationTime/1000, 'unixepoch', 'localtime') >= date('now', '-7 day', 'localtime') ORDER BY notificationTime DESC")
    fun getWeekOrders(): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE strftime('%Y-%m', notificationTime/1000, 'unixepoch', 'localtime') = strftime('%Y-%m', 'now', 'localtime') ORDER BY notificationTime DESC")
    fun getMonthOrders(): LiveData<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}