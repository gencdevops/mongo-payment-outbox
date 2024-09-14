package com.iyzico.challenge.repository;


import com.iyzico.challenge.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightId(Long flightId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)  // Kilitleme stratejisi: Aynı anda yazmaya izin verme
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Seat findByIdForUpdate(@Param("id") Long id);
}

    /*
 1. @Lock Anotasyonu ve Kullanımının Değerlendirilmesi

    @Lock anotasyonu, Spring Data JPA'da eşzamanlılık kontrolünü sağlamak için kullanılabilir. İki tip kilit stratejisi vardır:

    PESSIMISTIC_READ: Diğer işlemlerin veriyi okumasına izin verir ancak güncelleme yapmasını engeller.
    PESSIMISTIC_WRITE: Diğer işlemlerin ne okumasına ne de yazmasına izin verir.
Burada, idempotency sağlamak ve aynı koltuğun iki kez satılmasını engellemek için pessimistic locking stratejileri etkili
 olabilir. Zaten @Version kullanılarak optimistic locking stratejisi uygulanmıştı. Ancak bu yaklaşım, yazma işlemleri
  sırasında çakışmaları çözmek yerine önceden engellemek amacıyla PESSIMISTIC_WRITE kilitlemesiyle güçlendirilebilir.

    @Lock Anotasyonu Kullanımı
    SeatRepository üzerinde @Lock anotasyonunu uygulayarak koltuğa aynı anda erişim denemelerinin kontrolünü sağlayabiliriz.


    Neden @Lock(LockModeType.PESSIMISTIC_WRITE) Kullandık?
@Lock(LockModeType.PESSIMISTIC_WRITE) stratejisini kullanmamızın nedeni, eşzamanlı yazma işlemlerini önleyerek aynı koltuğun
birden fazla kişiye satılmasını engellemek istememizdir. Pessimistic Write Lock, veriyi okurken ve güncellerken diğer
 işlemlerin bu veriye erişmesini tamamen engeller. Yani, aynı anda birden fazla kullanıcı koltuk satın alma talebi yaparsa,
  sistem bu taleplerden sadece birini işleyip diğerlerini engeller.

Alternatif olarak, @Lock(value = LockModeType.OPTIMISTIC) (Optimistic Locking) kullanabilirdik. Ancak Optimistic Locking
 şu şekilde çalışır:

İşlemler sırasında veri üzerinde bir kilit tutulmaz. Her işlem veri okuduktan sonra, güncellemeye çalışırken verinin
değişip değişmediğini kontrol eder.
Eğer verinin "version" değeri değişmişse, yani başka bir işlem o veriyi güncellemişse, o zaman işlem başarısız olur
ve bir hata fırlatılır.
Bu senaryoda Optimistic Lock kullanmamanın sebebi:

Optimistic Lock, güncellemeler sırasında veri çakışmasını sonradan fark eder. Eğer aynı koltuğu iki kişi satın
almaya çalışıyorsa, ikinci kişi ancak güncelleme sırasında hata alır. Bu da kullanıcı deneyimi açısından sorun
yaratabilir, çünkü ikinci kullanıcı ancak işlemin sonunda hatayla karşılaşır.
Pessimistic Lock ise aynı anda birden fazla yazma işlemi olmasını en baştan engelleyerek veritabanı üzerinde kesin
bir kontrol sağlar.



Gereksinimlerin Sağlanması
A seat should not be sold to two passengers.

Bu gereksinimi sağlamak için, aşağıdaki iki önemli strateji uygulanmıştır:

Pessimistic Locking (Kilitleme): @Lock(LockModeType.PESSIMISTIC_WRITE) anotasyonu, SeatRepository'de koltuğa
 erişim sırasında bir kilit uygulayarak aynı anda birden fazla işlem yapmayı engeller. Bu kilitleme, aynı
 koltuğun birden fazla kullanıcı tarafından satın alınmasını önler.
Availability Check: Koltuğun isAvailable durumu kontrol edilerek, eğer koltuk zaten satılmışsa ödeme işlemi engellenir.
If there are 2 passengers pay at the same time for the same seat, first successful should buy the seat and
 the 2nd one should fail with an appropriate message.

Bu gereksinimi karşılamak için:

Pessimistic Locking: Kilitlenmiş verilerle yapılan işlemler sırasında, sadece ilk başarılı ödeme işlemi koltuğu alabilir. Diğer işlemler kilit nedeniyle beklemeye alınır veya başarısız olur.
*/








