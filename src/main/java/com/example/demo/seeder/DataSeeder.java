package com.example.demo.seeder;

import com.example.demo.enums.RoomStatus;
import com.example.demo.models.Amenity;
import com.example.demo.models.FoodItem;
import com.example.demo.models.Room;
import com.example.demo.repository.AmenityRepository;
import com.example.demo.repository.FoodItemRepository;
import com.example.demo.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final FoodItemRepository foodItemRepository;

    public DataSeeder(RoomRepository roomRepository, AmenityRepository amenityRepository, FoodItemRepository foodItemRepository) {
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedAmenitiesAndRooms();
        seedFoodMenu();
    }

    private void seedAmenitiesAndRooms() {
        if (roomRepository.count() < 20) {
            System.out.println("Seeding amenities and rooms...");

            // 1. Seed Amenities
            List<String> amenityNames = List.of(
                    "Free Wi-Fi",
                    "Air Conditioning",
                    "Flat-screen TV",
                    "Mini Bar",
                    "Balcony",
                    "Swimming Pool Access",
                    "Gym Access",
                    "Complimentary Breakfast",
                    "Room Service",
                    "Coffee Maker"
            );

            Set<Amenity> allAmenities = new HashSet<>();
            for (String name : amenityNames) {
                Amenity amenity = new Amenity();
                amenity.setName(name);
                amenityRepository.findByName(name).ifPresentOrElse(
                        allAmenities::add,
                        () -> allAmenities.add(amenityRepository.save(amenity))
                );
            }

            // Convert set to array for easier random selection, or just pick sub-sets manually
            Amenity[] amenityArr = allAmenities.toArray(new Amenity[0]);

            // 2. Seed 20 Rooms
            // 5 Standard (1st floor), 5 Deluxe (2nd floor), 5 Suite (3rd floor), 5 Executive (4th floor)

            String[] types = {"Standard", "Deluxe", "Suite", "Executive"};
            Double[] prices = {100.0, 200.0, 350.0, 500.0};
            int[] maxGuests = {2, 3, 4, 2};

            for (int floor = 1; floor <= 4; floor++) {
                String currentType = types[floor - 1];
                Double currentPrice = prices[floor - 1];
                int currentMaxGuest = maxGuests[floor - 1];

                for (int i = 1; i <= 5; i++) {
                    Room room = new Room();
                    String rNumber = "S-" + floor + "0" + i;
                    if (roomRepository.findByRoomNumber(rNumber).isPresent()) continue;
                    room.setRoomNumber(rNumber);
                    room.setRoomType(currentType);
                    room.setStatus(RoomStatus.AVAILABLE);
                    room.setMaxGuest(currentMaxGuest);
                    room.setFloor(floor);
                    room.setPricePerNight(currentPrice);

                    // Add some random amenities
                    Set<Amenity> roomAmenities = new HashSet<>();
                    roomAmenities.add(amenityArr[0]); // Everyone gets Wi-Fi
                    roomAmenities.add(amenityArr[1]); // Everyone gets AC

                    if (floor >= 2) {
                        roomAmenities.add(amenityArr[2]); // TV
                        roomAmenities.add(amenityArr[9]); // Coffee Maker
                    }
                    if (floor >= 3) {
                        roomAmenities.add(amenityArr[3]); // Mini Bar
                        roomAmenities.add(amenityArr[4]); // Balcony
                        roomAmenities.add(amenityArr[7]); // Breakfast
                    }
                    if (floor == 4) {
                        roomAmenities.add(amenityArr[5]); // Pool
                        roomAmenities.add(amenityArr[6]); // Gym
                        roomAmenities.add(amenityArr[8]); // Room Service
                    }

                    room.setAmenities(roomAmenities);
                    roomRepository.save(room);
                }
            }

            System.out.println("Seeding completed: Added 20 rooms with amenities.");
        } else {
            System.out.println("Rooms already exist. Skipping seeding.");
        }
    }

    private void seedFoodMenu() {
        if (foodItemRepository.count() == 0) {
            System.out.println("Seeding food menu...");

            List<FoodItem> items = List.of(
                    createItem("Margherita Pizza", "Classic tomato, mozzarella, basil", "Main Course", 350.0, "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400&h=300&fit=crop"),
                    createItem("Chicken Biryani", "Fragrant basmati rice with spiced chicken", "Main Course", 450.0, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400&h=300&fit=crop"),
                    createItem("Caesar Salad", "Fresh romaine, parmesan, croutons", "Salads", 280.0, "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400&h=300&fit=crop"),
                    createItem("Chocolate Brownie", "Warm fudgy brownie with vanilla ice cream", "Desserts", 180.0, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400&h=300&fit=crop"),
                    createItem("French Fries", "Crispy golden fries", "Sides", 150.0, "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop"),
                    createItem("Grilled Chicken", "Herb-marinated grilled chicken breast", "Main Course", 420.0, "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=400&h=300&fit=crop"),
                    createItem("Vegetable Soup", "Fresh seasonal vegetables in broth", "Soups", 200.0, "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop"),
                    createItem("Tiramisu", "Classic Italian dessert", "Desserts", 250.0, "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400&h=300&fit=crop"),
                    createItem("Garlic Bread", "Toasted bread with garlic butter", "Sides", 120.0, "https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?w=400&h=300&fit=crop"),
                    createItem("Pasta Carbonara", "Creamy pasta with bacon and parmesan", "Main Course", 380.0, "https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop"),
                    createItem("Fresh Orange Juice", "Freshly squeezed orange juice", "Beverages", 100.0, "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400&h=300&fit=crop"),
                    createItem("Cappuccino", "Espresso with steamed milk foam", "Beverages", 150.0, "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400&h=300&fit=crop")
            );

            foodItemRepository.saveAll(items);
            System.out.println("Seeding completed: Added " + items.size() + " food menu items.");
        } else {
            System.out.println("Food menu already exists. Skipping seeding.");
        }
    }

    private FoodItem createItem(String name, String description, String category, Double price, String imageUrl) {
        FoodItem item = new FoodItem();
        item.setName(name);
        item.setDescription(description);
        item.setCategory(category);
        item.setPrice(price);
        item.setAvailable(true);
        item.setImageUrl(imageUrl);
        return item;
    }
}
