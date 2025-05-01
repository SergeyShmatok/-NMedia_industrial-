package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import java.io.IOException
import javax.inject.Inject


class PostPagingSource @Inject constructor(
    private val apiService: ApiService,
    private val dao: PostDao
) : PagingSource<Long, Post>() {

    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null // Нужна для обновления
    // данных по определённому ключу (тут использоваться не будет).

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> { // LoadParams - sealed класс.

        try {
            val result = when (params) {

                is LoadParams.Refresh -> apiService.getLatest(params.loadSize)
                // Пользователь "скроллит вниз".
                is LoadParams.Append -> apiService.getBefore(
                    id = params.key,
                    count = params.loadSize,
                )

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


            }

            if (!result.isSuccessful)  throw HttpException(result)

            val data = result.body().orEmpty()

            return LoadResult.Page(
                data = data,
                prevKey = params.key,
                nextKey = data.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }


}