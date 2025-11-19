package com.cowatracker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.cowatracker.app.data.AppDatabase
import com.cowatracker.app.data.Order
import com.cowatracker.app.data.OrderDao
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val orderDao: OrderDao

    init {
        val database = AppDatabase.getDatabase(application)
        orderDao = database.orderDao()
    }

    fun getAllOrders(): LiveData<List<Order>> {
        return orderDao.getAllOrders()
    }

    fun getTodayOrders(): LiveData<List<Order>> {
        return orderDao.getTodayOrders()
    }

    fun getWeekOrders(): LiveData<List<Order>> {
        return orderDao.getWeekOrders()
    }

    fun getMonthOrders(): LiveData<List<Order>> {
        return orderDao.getMonthOrders()
    }

    fun insertOrder(order: Order) {
        viewModelScope.launch {
            orderDao.insertOrder(order)
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            orderDao.updateOrder(order)
        }
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            val order = orderDao.getOrderById(orderId)
            order?.let {
                val updated = it.copy(
                    status = status,
                    lastModified = System.currentTimeMillis()
                )
                orderDao.updateOrder(updated)
            }
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            orderDao.deleteOrder(order)
        }
    }

    fun deleteAllOrders() {
        viewModelScope.launch {
            orderDao.deleteAllOrders()
        }
    }
}