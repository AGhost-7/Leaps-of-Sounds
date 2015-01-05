module.exports = function(grunt) {
	
	var 
		files = [
			'src/constructor.js',
			'src/utils/*.js',
			'src/models/*.js',
			'src/views/*.js'
		],
		concatOptions = {
			separator: ";\n"
		}
		
	grunt.initConfig({
		concat: {
			default: {
				files: {
					'dist/fingerboard.js': files
				},
				options: concatOptions
			},
			play: {
				files: {
					'../public/javascripts/fingerboard.js': files
				},
				options: concatOptions
			}
		},
		uglify: {
			default: {
				files: {
					'dist/fingerboard.min.js': ['dist/fingerboard.js']
				}
			},
			play: {
				files: {
					'../public/javascripts/fingerboard.min.js': ['../public/javascripts/fingerboard.js']
				}
			}
		},
		watch:{
			default:{
				files: ['src/**/*.js'],
				tasks: ['concat']
			},
			play:{
				files: ['src/**/*.js'],
				tasks: ['concat:play']
			}
			
		}
	});
	
	
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-coffee');
	
	grunt.registerTask('default', ['concat', 'uglify']);
	grunt.registerTask('play', ['concat:play', 'uglify:play']);
	//grunt.registerTask('local-testing', [''])
};