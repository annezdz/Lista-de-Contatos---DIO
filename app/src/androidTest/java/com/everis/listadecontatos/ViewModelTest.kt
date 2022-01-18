package com.everis.listadecontatos

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.everis.listadecontatos.feature.listacontatos.model.ContatosVO
import com.everis.listadecontatos.feature.listacontatos.repository.ListaDeContatosRepository
import com.everis.listadecontatos.feature.listacontatos.viewModel.ListaDeContatosViewModel
import com.everis.listadecontatos.helpers.HelperDB

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ViewModelTest {

    lateinit var repository : ListaDeContatosRepository
    lateinit var viewModel: ListaDeContatosViewModel

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        repository = ListaDeContatosRepository(
            HelperDB(appContext)
        )
    }

    // criando o mock

    fun setupMock() {
        repository = Mockito.mock(ListaDeContatosRepository::class.java)
        Mockito.`when`(repository.requestListaDeContatos(
            Mockito.anyString(),
            MockitoHelper.anyObject(),
            MockitoHelper.anyObject()
        )).thenAnswer {
            val onSucesso = it.arguments[1] as ((List<ContatosVO>) -> Unit)
            val lista = mutableListOf<ContatosVO>()
            lista.add(ContatosVO(1,"Teste unitario","12345678"))
            onSucesso?.invoke(lista)

            ""
        }
    }


    @Test
    fun viewModelTest() {
        setupMock()
        viewModel= ListaDeContatosViewModel(
            repository
        )
        var lista : List<ContatosVO>?= null
        var lock = CountDownLatch(1)
        viewModel.getListaDeContatos(
            "",
            onSucesso =  { listaResult  ->
                lista = listaResult
                lock.countDown() // aguarda retorno assíncrono

            }, onError =  { ex ->
                fail("Nao deveria retornar erro")
                lock.countDown()
            }
        )
        lock.await(3000, TimeUnit.MILLISECONDS)
        assertNotNull(lista)
        assertTrue(lista!!.isNotEmpty())
        assertEquals(1, lista!!.size)
    }
}

object MockitoHelper {
    fun <T> anyObject(): T
    {
        Mockito.any<T>()
        return uninitialized()
    }
    @Suppress("UNCHECKED_CAST")
    fun <T> uninitialized(): T = null as T
}
