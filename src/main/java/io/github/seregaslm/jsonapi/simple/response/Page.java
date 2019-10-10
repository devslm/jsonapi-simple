package io.github.seregaslm.jsonapi.simple.response;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Class using when need pagination.
 *
 * <p>When we need pagination we can use this class as return type for our services
 * and then in controllers we can retrieve data object, total size of items
 * and pagination links for example:
 * <pre>
 *     // Some service lass
 *     public Page<Dto> getItems() {
 *         return new Page<>()
 *             .setData(new Dto())
 *             .setTotal(123)
 *             .setNext("next-page-link");
 *     }
 *
 *     // Some controller class
 *     public Response<Dto> getItems() {
 *         final Page<Dto> page = service.getItems();
 *
 *         return Response.<Dto, Dto>builder()
 *             .data(page.getData())
 *             .total(page.getTotal())
 *             .pageNext(page.getNext())
 *             .build();
 *     }
 * </pre>
 * @param <T>
 */
@Setter
@Accessors(chain = true)
public class Page<T> {
    private T data;
    private String prev;
    private String next;
    private long total;
}
