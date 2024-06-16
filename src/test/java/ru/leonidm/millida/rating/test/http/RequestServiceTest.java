package ru.leonidm.millida.rating.test.http;

import org.junit.jupiter.api.Test;
import ru.leonidm.millida.rating.service.MillidaRatingRequestService;

public class RequestServiceTest {

    @Test
    void test() {
        MillidaRatingRequestService requestService = new MillidaRatingRequestService(87);

        System.out.println("[RequestServiceTest:12] requestService.fetch(1): " + requestService.fetch(1));
        System.out.println("[RequestServiceTest:13] requestService.fetch(2): " + requestService.fetch(2));
        System.out.println("[RequestServiceTest:14] requestService.fetch(3): " + requestService.fetch(3));
        System.out.println("[RequestServiceTest:15] requestService.fetch(4): " + requestService.fetch(4));
        System.out.println("[RequestServiceTest:16] requestService.fetch(5): " + requestService.fetch(5));

        System.out.println("[RequestServiceTest:18] requestService.topDay(): " + requestService.topDay());
        System.out.println("[RequestServiceTest:19] requestService.topWeek(): " + requestService.topWeek());
        System.out.println("[RequestServiceTest:20] requestService.topMonth(): " + requestService.topMonth());
    }
}
