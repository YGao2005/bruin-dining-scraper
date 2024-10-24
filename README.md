<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a name="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->

[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<div align="center">
  <h3 align="center">Bruin Dining Scraper API</h3>
  <p align="center">
    An API designed for the Bruin Dining app that scrapes data from UCLA menus
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul> 
        <li>
           <a href="#getting-all-menu-items-by-name"# >Getting all menu items by name </a> 
        </li>
        <li>
           <a href="#getting-all-menu-items-by-id"# >Getting all menu items by ID </a> 
        </li>
        <li>
           <a href="#getting-theme"# >Getting Theme </a> 
        </li>
        <li>
           <a href="#autosuggestion-and-search-helper"# >Autosuggestion and Search Helper </a> 
        </li>
        <li>
          <a href="#get-menu-formats-by-date-and-meal-period"# >Get Menu Formats By Date and Meal Period</a>
        </li>
      </ul>
    </li>
    <li>
           <a href="#Roadmap"# >Roadmap </a> 
    </li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

API Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com`

The purpose of this API is to provide a convenient and efficient way for users to access and interact with dining hall menu information from UCLA. The data collected by the API aims to allow Bruin Dining to create a more user-friendly and accessible experience. 

Some key features:
* Scrapes data from UCLA dining hall menus automatically every day or on user demand and stores it into a database
* Allows for users to search for all menu items that contain certain words
* Allows for users to filter menu items depending on dietary restrictions
* Allows for users to get dates upcoming dates when a certain menu item will be offered
* Scrapes data automatically at 10PM every day

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

I coded the API in Java using a Spring Boot framework and stored the data in a MySQL databased hosted on Aiven. I hosted the API using Heroku. 

* [![Java][Java-img]][Java-url]
* [![Spring][Spring-img]][Spring-url]
* [![MySQL][MySQL-img]][MySQL-url]
* [![Heroku][Heroku-img]][Heroku-url]


<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- USAGE EXAMPLES -->
## Usage

Here are all the usable API routes and their function. 



### Getting all menu item by name

Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getallitemsbyname?name={NAME}`

Replace `{NAME}` with the name of the menu item you would like to search for.

This call will return a list of menu items in a json format (see below) that specifies: 
1. Menu item ID
2. Item name
3. Link to the nutritional page
4. One instance of a meal period when it is offered
5. The restaurant the item is offered at
6. One instance of a date where the menu item is offered (between the dates of yesterday and the 7 days from today)
7. A list of the health restrictions of the item

Example of returned json with route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getallitemsbyname?name=Cheese Pizza`

```
   {
        "id": 936,
        "itemName": "Cheese Pizza",
        "nutritionalLink": "https://menu.dining.ucla.edu/Recipes/400082/1!10",
        "mealPeriodName": "Lunch",
        "restaurantName": "De Neve",
        "sectionName": "The Pizzeria",
        "date": "2024-04-27",
        "healthRestrictions": [
            {
                "id": 1,
                "name": "AWHT"
            },
            {
                "id": 2,
                "name": "AGTN"
            },
            {
                "id": 6,
                "name": "V"
            },
            {
                "id": 8,
                "name": "AMLK"
            }
        ]
    }
   ```

### Getting all menu items by ID

Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getallitemsbyid?id={ID}`

Replace `{ID}` with the ID of the menu item that you would like to search for. 

This call will return a list of menu items in a json format (see below) that specifies: 
1. Menu item ID
2. Item name
3. Link to the nutritional page
4. One instance of a meal period when it is offered
5. The restaurant the item is offered at
6. One instance of a date where the menu item is offered
7. A list of the health restrictions of the item

Example usage to produce the same json output as above: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getallitemsbyid?id=812`

### Getting theme

Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/gettheme?date={DATE}&mealPeriod={MEALPERIOD}`

Replace `{DATE}` with the format of YYYY-MM-DD and {MEALPERIOD} with either "Breakfast", "Lunch", or "Dinner".

This call will return the De Neve theme of that specific lunch and meal period, or nothing otherwise. 

Example usage: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/gettheme?date=2024-04-26&mealPeriod=Lunch`
```
LA Street Food
```

### Autosuggestion and Search Helper

Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/search?query={QUERY}`

Replace `{QUERY}` with the search query.

This call will return all menu items containing the query inside their name. This includes the menu item name, restaurant name, menu item ID, and list of health restrictions as depicted below. The search feature is case-insensitive.  

Example usage: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/search?query=cheese`

```
{
        "menuItemName": "Three Cheese & Tomato Pizza",
        "restaurantName": "De Neve",
        "menuItemId": 1745,
        "healthRestrictions": [
            "AWHT",
            "AGTN",
            "V",
            "AMLK"
        ]
}
```

### Get Menu Formats By Date and Meal Period

Route: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getmenuformatsbydateandmealperiod?date={DATE}&mealPeriod={MEALPERIOD}`

Replace `{DATE}` with the format of YYYY-MM-DD and {MEALPERIOD} with either "Breakfast", "Lunch", or "Dinner".

Returns the formatted menus of Bruin Plate, De Neve, and Epicuria. 

Formats in this structure (for 3 restaurants):

```
[
  {
    Restaurant,
    menuSections:
    {
      MenuSection1:
        {
          MenuItem:
            {
              MenuItemName,
              MenuItemID
            }
        }
    }
  }
]
```

Example usage: `https://bruin-menu-scraper-f710fcfa2eb4.herokuapp.com/api/menus/getmenuformatsbydateandmealperiod?date=2024-04-29&mealPeriod=Dinner`

Example of a section of the returned JSON: 
```
{
        "restaurantName": "De Neve",
        "menuSections": {
            "The Pizzeria": [
                {
                    "Garlic Chicken Pizza": 1609
                },
                {
                    "Margherita Pizza": 1612
                },
                {
                    "Pepperoni Deluxe Pizza": 1606
                }
            ],
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Roadmap

- [x] Add Changelog
- [x] Fixed bug that prevents dinner menu from being scraped
- [x] Search function now also returns menu item ID  
- [x] Fix scheduler so menus can be scraped daily
- [ ] Add scraping service for Feast


<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->
## Contact

Yang Gao - psdyangg@gmail.com

Portfolio: WIP

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.com/othneildrew/Best-README-Template/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=for-the-badge
[forks-url]: https://github.com/othneildrew/Best-README-Template/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=for-the-badge
[stars-url]: https://github.com/othneildrew/Best-README-Template/stargazers
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=for-the-badge
[issues-url]: https://github.com/othneildrew/Best-README-Template/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge
[license-url]: https://github.com/othneildrew/Best-README-Template/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/yang-gao-65ba61179/
[Java-img]: https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://www.java.com/
[MySQL-img]: https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white
[MySQL-url]: https://www.mysql.com/
[Heroku-img]: https://img.shields.io/badge/heroku-%23430098.svg?style=for-the-badge&logo=heroku&logoColor=white
[Heroku-url]: https://www.heroku.com
[Spring-img]: https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white
[Spring-url]: https://spring.io/
