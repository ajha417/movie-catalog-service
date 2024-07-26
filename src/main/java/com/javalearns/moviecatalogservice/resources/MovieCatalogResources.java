package com.javalearns.moviecatalogservice.resources;

import com.javalearns.moviecatalogservice.model.CatalogItem;
import com.javalearns.moviecatalogservice.model.Movie;
import com.javalearns.moviecatalogservice.model.Rating;
import com.javalearns.moviecatalogservice.model.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResources {

    @Autowired
    private RestTemplate restTemplate;  // here it will go to find method where it is annotated with @Bean and create instance of RestTemplate lazily

    @Autowired
    private WebClient.Builder webClientBuilder;
    @GetMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

//        List<Rating> ratings = Arrays.asList(
//                new Rating("1234", 4.4),
//                new Rating("3423", 3.4)
//        );

        UserRating ratings = restTemplate.getForObject("http://localhost:8083/ratingsdata/users/" + userId, UserRating.class);

        return ratings.getUserRatings().stream().map(rating -> {
//            Movie movie = restTemplate.getForObject("http://localhost:8082/movies/" + rating.getMovieId(), Movie.class);
            // for each movie ID, call movie info service and get details
            Movie movie = webClientBuilder.build()
                                        .get()
                                        .uri("http://localhost:8082/movies/" + rating.getMovieId())
                                        .retrieve()
                                        .bodyToMono(Movie.class)
                                        .block();
            //put them all together
            return new CatalogItem(movie.getName(), "Nice movie", rating.getRating());
        }).collect(Collectors.toList());

    }
}
