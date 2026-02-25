module Fastlane
  module Actions
    class ActPublishReleaseAction < Action
      def self.run(params)
        version = params[:version]
        remote = params[:remote]
        skip_bump = params[:skip_bump]
        gradle_properties_path = "gradle.properties"

        # Read current gradle.properties
        unless File.exist?(gradle_properties_path)
          UI.user_error!("gradle.properties file not found!")
        end

        properties_content = File.read(gradle_properties_path)

        # Determine version to use
        if version.nil? || version.empty?
          current_version_match = properties_content.match(/^VERSION=(.+)$/m)
          if current_version_match
            current_version = current_version_match[1].strip

            if skip_bump
              # Use current version as-is
              version = current_version
              UI.message("Using current version: #{version}")
            else
              # Auto bump the last number
              version_parts = current_version.split('.')

              # Remove -SNAPSHOT if present from last part
              last_part = version_parts.last.gsub('-SNAPSHOT', '')

              # Increment the last number
              if last_part.match(/^\d+$/)
                last_part = (last_part.to_i + 1).to_s
              else
                UI.user_error!("Last version part '#{last_part}' is not a number!")
              end

              version_parts[-1] = last_part
              version = version_parts.join('.')

              UI.message("Auto-bumped version from #{current_version} to #{version}")
            end
          else
            UI.user_error!("VERSION not found in gradle.properties!")
          end
        end

        # Update VERSION in gradle.properties
        new_content = properties_content.gsub(/^VERSION=.+$/m, "VERSION=#{version}")
        File.write(gradle_properties_path, new_content)

        UI.success("Updated VERSION to #{version} in gradle.properties")

        # Clean before publishing
        UI.message("Cleaning project...")
        sh("./gradlew clean")

        # Choose publish command based on remote parameter
        if remote
          UI.message("Publishing to remote repository...")
          gradle_command = "./gradlew publishReleasePublicationToNexusRepository"
        else
          UI.message("Publishing to local Maven repository...")
          gradle_command = "./gradlew publishToMavenLocal"
        end

        # Execute gradle publish command
        sh(gradle_command)

        UI.success("Successfully published version #{version}!")

        # Print the published version
        puts "Published version: #{version}"

        # Return the version for potential use in other actions
        version
      end

      def self.description
        "Publish version with automatic version bumping and remote/local publishing options"
      end

      def self.authors
        ["Core Team"]
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :version,
            description: "Version number (a.b.c.d format). If not provided, auto-bumps last number from gradle.properties",
            optional: true,
            type: String
          ),
          FastlaneCore::ConfigItem.new(
            key: :remote,
            description: "Whether to publish to remote repository (true) or local Maven (false)",
            optional: true,
            default_value: true,
            type: Boolean
          ),
          FastlaneCore::ConfigItem.new(
            key: :skip_bump,
            description: "Skip version bumping and use current version from gradle.properties",
            optional: true,
            default_value: false,
            type: Boolean
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end

      def self.example_code
        [
          'publish_version',
          'publish_version(version: "1.2.3", remote: false)',
          'publish_version(remote: true)',
          'publish_version(version: "2.0.0")',
          'publish_version(skip_bump: true)',
          'publish_version(skip_bump: true, remote: false)'
        ]
      end

      def self.category
        :building
      end
    end
  end
end