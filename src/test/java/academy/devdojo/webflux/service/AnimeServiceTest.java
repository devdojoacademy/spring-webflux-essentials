package academy.devdojo.webflux.service;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.repository.AnimeRepository;
import academy.devdojo.webflux.util.AnimeCreator;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepositoryMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        BDDMockito.when(animeRepositoryMock.findAll())
            .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createAnimeToBeSaved()))
            .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock
            .saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
            .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepositoryMock.delete(ArgumentMatchers.any(Anime.class)))
            .thenReturn(Mono.empty());

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createValidAnime()))
            .thenReturn(Mono.empty());
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0); //NOSONAR
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findAll())
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono with anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findById(1))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.save(animeToBeSaved))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete();
    }

    @Test
    @DisplayName("saveAll creates a list of anime when successful")
    public void saveAll_CreatesListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
            .expectSubscription()
            .expectNext(anime, anime)
            .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns Mono error when one of the objects in the list contains empty or null name")
    public void saveAll_ReturnsMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(animeRepositoryMock
            .saveAll(ArgumentMatchers.anyIterable()))
            .thenReturn(Flux.just(anime, anime.withName("")));

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved, animeToBeSaved.withName(""))))
            .expectSubscription()
            .expectNext(anime)
            .expectError(ResponseStatusException.class)
            .verify();
    }


    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemovesAnime_WhenSuccessful() {
        StepVerifier.create(animeService.delete(1))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("delete returns Mono error when anome does not exist")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }

}