package com.example.coroutinenoncancellable.component

import org.springframework.stereotype.Component

@Component
class SampleComponent {
    val intStorage = mutableListOf<Int>()

    fun store(int: Int) { intStorage.add(int) }
    fun clear() { intStorage.clear() }
    fun list(): List<Int> = intStorage.toList()

    suspend fun perform() { /* does nothing, for mocking */ }
}
