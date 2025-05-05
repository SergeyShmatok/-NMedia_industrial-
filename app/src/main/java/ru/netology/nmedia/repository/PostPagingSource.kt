package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toDto
import java.io.IOException
import javax.inject.Inject


class PostPagingSource @Inject constructor(
    private val apiService: ApiService,
    private val dao: PostDao
) : PagingSource<Long, Post>() {

    private var lastId: Long = 0

    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null // Нужна для обновления
    // данных по определённому ключу (тут использоваться не будет).

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> { // LoadParams - sealed класс.

        try {

            val list = when (params) {

                is LoadParams.Refresh -> {
                    delay(7000) // Задержка, чтобы успеть загрузить данные в Room с сервера (5 сек)
                    dao.getLatest(params.loadSize)}
                // Пользователь "скроллит вниз".
                is LoadParams.Append -> dao.getBefore(lastId, params.loadSize)

                // Пользователь "скроллит вверх".
                is LoadParams.Prepend -> return LoadResult.Page(
                    // Если не нужно обрабатывать какое-то
                    // событие.
                    data = emptyList(),
                    prevKey = params.key,
                    nextKey = null,
                )
                // Объект результата успеха для PagingSource.load.
                // Параметры:
                // data - Загруженные данные
                // prevKey - Ключ для предыдущей страницы, если в этом направлении можно загрузить
                // больше данных, в противном случае null.
                // nextKey - Ключ для следующей страницы, если в этом направлении можно загрузить
                // больше данных, в противном случае null.


            }.toDto()

//            if (!result.isSuccessful)  throw HttpException(result)

            lastId = list.lastOrNull()?.id ?: 0L

            return LoadResult.Page(
                data = list,
                prevKey = params.key,
                nextKey = list.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }


}